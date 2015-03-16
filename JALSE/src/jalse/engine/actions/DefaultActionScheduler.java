package jalse.engine.actions;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.TimeUnit;

/**
 * A {@link ActionScheduler} implementation that schedules all actions against the supplied actor.
 * Weak references are kept against all task IDs so they can be bulk cancelled (these are also
 * cleared on {@link ActionEngine} change).
 *
 * @author Elliot Ford
 *
 * @param <T>
 *            Actor type.
 */
public class DefaultActionScheduler<T> implements ActionScheduler<T> {

    private final T actor;
    private ActionEngine engine;
    private final Set<UUID> tasks;

    /**
     * Creates a DefaultScheduler for the supplied actor.
     *
     * @param actor
     *            Actor to schedule actions against.
     */
    public DefaultActionScheduler(final T actor) {
	this.actor = Objects.requireNonNull(actor);
	engine = null;
	tasks = Collections.newSetFromMap(new WeakHashMap<>());
    }

    @Override
    public boolean cancel(final UUID action) {
	return engine.cancel(action);
    }

    /**
     * Cancel all tasks scheduled to the current engine for the actor by this scheduler.
     */
    public void cancelTasks() {
	synchronized (tasks) {
	    for (final UUID id : tasks) {
		cancel(id);
	    }
	    tasks.clear();
	}
    }

    /**
     * Gets the action Actor.
     *
     * @return Actor to schedule events against.
     */
    public T getActor() {
	return actor;
    }

    /**
     * Gets the associated engine.
     *
     * @return Associated engine or null if it has not been set.
     */
    public ActionEngine getEngine() {
	return engine;
    }

    @Override
    public boolean isActive(final UUID action) {
	return engine.isActive(action);
    }

    @Override
    public UUID scheduleAction(final Action<T> action, final long initialDelay, final long period, final TimeUnit unit) {
	UUID task;
	synchronized (tasks) {
	    tasks.add(task = engine.scheduleAction(action, actor, initialDelay, period, unit));
	}
	return task;
    }

    /**
     * Associates a engine to this scheduler (if the engine changes all task references are lost).
     *
     * @param engine
     *            Engine to schedule actions against.
     */
    public void setEngine(final ActionEngine engine) {
	if (!Objects.equals(this.engine, engine)) {
	    synchronized (tasks) {
		tasks.clear();
	    }
	}
	this.engine = engine;
    }
}