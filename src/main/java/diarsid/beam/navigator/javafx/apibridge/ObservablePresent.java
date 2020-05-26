package diarsid.beam.navigator.javafx.apibridge;

import java.util.HashMap;
import java.util.Map;
import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

import diarsid.support.objects.references.Listening;
import diarsid.support.objects.references.real.Present;
import diarsid.support.objects.references.real.PresentListenable;

import static java.util.Objects.nonNull;

import static diarsid.support.objects.references.real.Presents.listenable;
import static diarsid.support.objects.references.real.Presents.listenablePresent;

public class ObservablePresent<T> implements ObservableValue<T> {

    private final PresentListenable<T> t;
    private final Map<ChangeListener<? super T>, Listening<T>> listeners;

    public ObservablePresent(PresentListenable<T> t) {
        this.t = t;
        this.listeners = new HashMap<>();
    }

    public ObservablePresent(Present<T> t, String name) {
        this.t = listenable(t, name);
        this.listeners = new HashMap<>();
    }

    public ObservablePresent(T t, String name) {
        this.t = listenablePresent(t, name);
        this.listeners = new HashMap<>();
    }

    @Override
    public void addListener(ChangeListener<? super T> listener) {
        Listening<T> listening = this.t.listen((oldT, newT) -> {
            listener.changed(this, oldT, newT);
        });
        this.listeners.put(listener, listening);
    }

    @Override
    public void removeListener(ChangeListener<? super T> listener) {
        Listening<T> listening = this.listeners.remove(listener);
        if ( nonNull(listening) ) {
            listening.cancel();
        }
    }

    @Override
    public T getValue() {
        return this.t.get();
    }

    @Override
    public void addListener(InvalidationListener listener) {
        // no need to implement
    }

    @Override
    public void removeListener(InvalidationListener listener) {
        // no need to implement
    }
}
