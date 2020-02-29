# SimpleMVP
Another implementation of MVP for Android that is built using:

* Proxy classes and reflection to handle presenter and view methods invocation
* Annotations to specify how to run presenter methods
* Executors to offload main thread and schedule periodic tasks

# Basics

* State holds a data to be rendered by view. After state change a copy is sent to connected views to update one's appearance.
* Presenter handles view events such as clicks, item selection and so on. It modifies state. Typically presenter lifetime goes beyond view lifetime.
* View updates itself after new state is received.
* Handler is an annotated presenter method to be explicitly or implicitly invoked by view

## State

State is inherited from `MvpState` class.

It keeps all data to update or restore view's appearance. It can be text to setup `TextView` or some boolean properties to enable/disable a particular `View`. Changed state is delivered to connected views when presenter handler invokes `commit()` method. 

State has special flag that indicates that it has been changed. It is important to update this flag after field change otherwise state is not delivered to connected views when `commit()` is called. In other words every field has to have setter to update this flag.

So a typical state may look following:

```java
public class MainState extends MvpState {
    public String text = "";
 
    public void setText(String text) {
       setChanged(!this.text.equals(text));
       this.text = text;
    }
}
```

View and presenter do not share the same state instance. `commit()` clones state before sending one to connected views so if a state contains a complex object or a collection then `clone()` method **must** be overridden. It performs a defencive copy of such object or collection.   

```java
public class MainState extends MvpState {
    public List<Event> events = new ArrayList<>();
 
    @Override
    public synchronized MainState clone() throws CloneNotSupportedException {
       MainState state = (MainState) super.clone();
       state.events = new ArrayList<>(events);
       return state;
    }
}
```

## Presenter

Presenters is inherited from `MvpBasePresenter` class.  

Usually presenter reacts to various events coming from a model or Android system to modify state. Modified state is sent to all connected views after `commit()` method calls.

Multiple views can share single presenter so all related business logic is placed in a single class.

Presenter handles various UI events such as:

* View clicks (`onViewClicked()` handler)
* Item selection (`onItemSelected()` handler)
* Text changes (`onTextChanged()` handler)

System events:

* Broadcast intents (`onBroadcastReceived()` handler)

There are following methods that reflect presenter lifetime:

* `onFirstViewConnected()` is called when first view is connected. It is a suitable place to allocate resources or subscribe to various model events.
* `onViewConnected()` is called when view is connected 
* `onViewsActive()` is called when at least one connected view has been started
* `onViewsInactive()` is called when all connected views have been stopped
* `onLastViewDisconnected()` is called when last view is disconnected. It is a place to release allocated resources.

Presenter stays alive on configuration change if one has been connected to `MvpActivity` instance.

Presenter handlers are annotated using `@MvpHandler` annotation to specify how to invoke handler. Annotation has following fields:

* `executor` - if true then run handler on executor to offload main thread (true by default)

There are several methods to initiate state delivery:

* `commit()` immediately sends state to all connected view
* `commit(long millis)` sends state after a short delay in milliseconds

In both cases the state **must** be changed (see the `setChanged()` and `isChanged()` methods)

Also there are following methods to submit and schedule tasks:

* `submit()` - submits task to execution immediately
* `schedulePeriodic()` - schedules periodic task that fires at fixed rate
* `schedule()` - schedules single shot task

Presenter does not hold strong reference to connected view. It collects `MvpViewHandle` instance that encapsulates weak reference to view so if view is suddenly destroyed (`onDestroy` method is not invoked) then presenter disconnects itself from a such view. Presenter interacts with a view using `MvpViewHandle` class reference that provides following methods:

* `getArguments()` returns an argument bundle
* `showToast()` to show a toast with a specified text
* `showSnackBar()` to show a snackbar with a specified text and action (optional)
* `startActivity()` to start a new activity
* `startActivityForResult()` to start an activity for result (calling view **must** be an activity)
* `showDialog()` to show a dialog
* `finish()` to finish the calling view

`MvpViewHandle` is passed to all default handlers. It is a best practice when custom handler is added.

In some cases it is not suitable to use `MvpViewHandle` of the calling view if one is about to be destroyed for example. `getParentHandle()` method provides a way to perform an action on behalf of parent view in such case.   

## View

There are multiple `MvpView` implementations to inherit from:

* `MvpActivity`
* `MvpFragment`
* `MvpDialogFragment`

Every view has to implement following methods:

* `getLayoutId()` returns layout ID to be inflated
* `onStateChanged()` updates views state
* `onInitPresenter()` creates new or gets existing presenter

If view has a menu then `getMenuId()` method should be overridden to provide menu ID.

`onStateChanged` method is called when new state is received. Views appearance is updated in this method, e.g. controls are enabled or disabled, text is changed and so on. Some views or adapters do not need to be updated so frequently. `onFirstStateChange` method is preferable in such case because it is called only once when view becomes ready. Both methods invocation is affected by view's lifecycle so if view is paused, for example, then methods are not invoked but queued to be called later when view becomes ready. View becomes ready when it is resumed and menu is inflated if it has one so it is safe to update menu items from both methods.

Also `onFirstStateChange` method is a safe place to setup listeners and watchers. There are several ways to do it:

* using `getMvpListener()` method
* using `newTextWatcher()`, `newQueryTextListener()`, `newOnPageChangeListener()`, `getMvpClickListener()` methods  

`getMvpListener()` method returns unified listener that is suitable for most cases. It handles clicks, checks and so on (see details in `MvpListener` interface declaration).

`newTextWatcher()` creates watcher that handles text changes of `EditText` view. `newQueryTextListener()` creates listener that handles `SearchView` text change. `newOnPageChangeListener()` creates listener that handles page selection of `ViewPager`. All these listeners and watcher are implicitly unregistered when view is stopped.

`getMvpClickListener()` method returns click listener that disables a view after click. It is handy in some cases to prevent user from starting multiple async operations.

`onInitPresenter` method is called when presenter initialization is required (view has been just created and has no presenter instance reference). `MvpPresenterManager` reference is passed to this method to instantiate new presenter or lookup existing presenter by ID. Typically parent view creates presenter instance which ID is shared with child views. `MvpFragment` and `MvpDialogFragment` have `initArguments` method to initialize arguments bundle with presenter ID. `MvpDialogFragment` looks up for presenter instance implicitly.

## Error handling

Every presenter handler invocation is implicitly wrapped by try-catch statement so application does not crash if something bad happens. Thrown exception is passed to exception handler to be logged or displayed as notification.

Custom error handler can be installed using `MvpPresenterManager::initialize` method. Application class is most suitable place to do it.

# Custom presenter handlers

New interface should be inherited from `MvpPresenter` to refer presenter instance that has custom handlers. This new interface has to be implemented by presenter class.

Interface:

```java
public interface MainPresenter extends MvpPresenter<MainState> {
    void customHandler(MvpViewHandle<MainState> handle);
}
```

Implementation:

```java
public class MainPresenterImpl extends MvpBasePresenter<MainState> implements MainPresenter {
    public MainPresenterImpl(Context context, MainState state) {
        super(context, state);
    }

    @Override
    @MvpHandler
    public void customHandler(MvpViewHandle<MainState> handle) {
      // do something
    }
}
```


View:

```java
public class MainActivity extends MvpActivity<MainPresenter, MainState> {

    @Override
    protected void onStart() {
        super.onStart();
        presenter.customHandler(getViewHandle());
    }

    @Override
    public MyPresenter onInitPresenter(MvpPresenterManager manager) {
        return manager.newPresenterInstance(MainPresenterImpl.class, MainState.class);
    }

}
```

# Pros and cons

## Pros

* view lifecycle is simplified
* all business logic is placed in a single class
* presenter handlers run on standalone thread (no problem with updating DB entries and so on)
* error handling

## Cons
* there is still no way to perform very long operations from presenter handlers (such as network requests).
* state's `clone()` method must be overridden in some cases   
* `EditText` can not be updated from `onStateChanged`
* `RecyclerView` adapter must enable stable ID feature

If `EditText` is updated from `onStateChanged` then endless cycle of `onTextChanged` and `onStateChanged` occurs. In other words there is no way to update `EditText` text without `MvpTextWatcher` invocation. It is better to set a text once from `onFirstStateChange` or use `MvpEditText` implementation that provides `setTextNoWatchers` method.    
  
# Test application

Test application demonstrates how various view events are processed by presenter. Every new event is logged to be displayed in UI.

There are following fragments:

* Main fragment
* Timer fragment
* Events fragment
* Settings dialog
* Event info dialog

Main fragment has several controls to show an android toast or a snackbar. Duration and text can be changed. Also there is a math expression calculator, a permissions request and a custom handler invocation. Pay attention how incorrect input and undefined mathematical operations are handled. 

Timer fragment provides a simple timer UI. There is stop/start button and elapsed time indicator. Timer is implemented using custom view (`CircleProgress`). Progress and state are saved between application startup. 

Event fragment displays all logged events. Every card has an ID, an event title and some info (resource name of view that produced an event, broadcast intent action). When floating action button is pressed all events are cleared. Precise event may be removed by pressing trashcan icon on the right side of the card. 

Settings dialog provides a control over UI update delay and allows to subscribe to several broadcast events.  

New DB entry is inserted each time presenter handler is invoked. Also some handlers commit data to shared preferences.  

Presenter lifetime is not affected by a configuration change so fragment's appearance is fully restored when configuration change has been finished.

# License
MIT License
Copyright (c) 2019-2020 Pavel Sokolov
