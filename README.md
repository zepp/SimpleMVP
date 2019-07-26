# SimpleMVP
Another implementation of MVP for Android that is built using:

* Proxy classes to handle presenter handlers invocation
* Annotations to specify how to run presenter handlers
* Executor to offload main thread

# Basics

* State (a class that is inherited from the `MvpState` class) holds a data to be rendered by view. After state change a copy is sent to connected views to update ones appearance.
* Presenter handles view events such as clicks, item selection and so on. It alters state. Typically presenter lifetime goes beyond view lifetime.
* View updates itself after new state is received. Also view implicitly invokes presenter handlers.
* Handler is an annotated presenter method to be invoked by view.

## Presenter

Usually presenter reacts to various event comming from a model or Android system to alter state. Altered state is sent to all connected views after `commit` method call.

Multiple views can share single presenter so all handlers are placed in a single class.

Presenter handles various UI events such as:

* View clicks (`onViewClicked` handler)
* Item selection (`onItemSelected` handler)
* Text changes (`onTextChanged` handler)

Presenter stays alive on configuration change if one has been connected to `MvpActivity`.

Presenter handlers are annotated using `@MvpEventHandler` annotation to specify how to invoke handler. Annotation has following fields:

* `executor` - if true then run handler on executor to offload main thread
* `sync` - if true then presenter is synchronized before handler invocation. It is better to leave default value.

## View

There are multiple `MvpView` implementations to inherit from:

* `MvpActivity`
* `MvpFragment`
* `MvpDialogFragment`

Every view has to implement following methods:

* `getLayoutId` returns layout ID to be inflated
* `getMenuId` returns menu ID to be inflated. 0 is returned if fragment or dialog does not have a menu.
* `onStateChanged`
* `onInitPresenter`

`onStateChanged` method is called when new state is received. In this method view appearance is updated, e.g. controls are enabled or disabled, text is changed. Method invocation is affected by view lifecycle so if view is paused then method is not invoked. When view is paused, new state is queued to be used later so it is not lost.

`onInitPresenter` method is called when presenter initialization is required (view has no valid presenter reference). `MvpPresenterManager` reference is passed to this method to instantiate new presenter or lookup existing presenter by ID.

## Error handling

Every presenter handler invocation is wrapped by try-catch statement so application does not crash if something bad happens. Thrown exception is logged.

# Custom presenter handlers

If you want to implement presenter that has custom handlers to be invoked then new interface should be inherited from`MvpPresenter` to refer presenter instance. This new interface is to be implemented by presenter class.

Presenter interface:

```java
public interface MyPresenter extends MvpPresenter<MainState> {
    void doSomething();
}
```

Activity:

```java
public class MainActivity extends MvpActivity<MyPresenter, MainState> {

    @Override
    protected void onStart() {
        super.onStart();
        presenter.doSomething();
    }

    @Override
    public MyPresenter onInitPresenter(MvpPresenterManager manager) {
        return manager.newPresenterInstance(MainPresenterImpl.class, MainState.class);
    }

}
```

Presenter:

```java
public class MainPresenterImpl extends MvpBasePresenter<MainState> implements MyPresenter {
    public MainPresenter(Context context, MainState state) {
        super(context, state);
    }

    @MvpEventHandler
    void doSomething() {
    }
}
```

# Test application

Test application demonstrates how various view events are processed by presenter. Every new event is logged to be displayed in UI.

There are three fragments:

* Main fragment
* Event fragments
* Settings dialog

Main fragment has several controls to show an android toast or a snackbar. Duration and text can be changed. Pay attention that in case of continuous input `onTextChanged` handler is invoked only once.

Event fragment displays all logged events. Every card has an ID, event title and resource name of view that produced an event. When floating action button is pressed all events are cleared. Precise event may be removed by pressing trashcan icon on the right side of the card. When new view is connected or disconnected an according event is displayed.

Settings dialog is just a mock-up that has radio button group and one switch. It is shown when toolbar gear icon is pressed. Controls state is saved to state so reopen does not change dialog appearance. Dialog lifetime affects when when `onViewConnected` and `onViewDisconnected` events are displayed.

Presenter lifetime is not affected by configuration change so fragments appearance is fully restored when configuration change has been finished.

# License
MIT License
Copyright (c) 2019 Pavel Sokolov
