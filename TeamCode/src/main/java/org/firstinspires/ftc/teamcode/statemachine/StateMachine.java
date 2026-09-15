package org.firstinspires.ftc.teamcode.statemachine;

import com.pedropathing.ivy.Command;
import com.pedropathing.ivy.behaviors.BlockedBehavior;
import com.pedropathing.ivy.behaviors.ConflictBehavior;
import com.pedropathing.ivy.behaviors.EndCondition;
import com.pedropathing.ivy.behaviors.InterruptedBehavior;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BooleanSupplier;

/**
 * A small, enum-keyed state machine that participates in Ivy as a single command.
 *
 * @param <S> enum used to identify states
 */
public final class StateMachine<S extends Enum<S>> implements Command {
    private final Map<S, Command> states = new LinkedHashMap<>();
    private final Map<S, List<Transition<S>>> transitions = new LinkedHashMap<>();
    private final Set<Object> requirements = new LinkedHashSet<>();

    private S initialState;
    private S currentState;
    private S previousState;
    private boolean currentCommandEnded;
    private boolean complete;
    private boolean configurationLocked;

    /**
     * Registers a command as a state.
     */
    public StateMachine<S> addState(S id, Command command) {
        ensureConfigurationIsMutable();
        Objects.requireNonNull(id, "State id cannot be null");
        Objects.requireNonNull(command, "State command cannot be null");

        if (states.containsKey(id)) {
            throw new IllegalArgumentException("State already exists: " + id);
        }

        states.put(id, command);
        transitions.put(id, new ArrayList<>());
        requirements.addAll(command.requirements());
        return this;
    }

    /**
     * Selects the state entered whenever this machine starts.
     */
    public StateMachine<S> setInitialState(S id) {
        ensureConfigurationIsMutable();
        requireRegisteredState(id);
        initialState = id;
        return this;
    }

    /**
     * Adds a predicate-driven transition. Transitions are evaluated in registration order.
     */
    public StateMachine<S> addTransition(S from, S to, BooleanSupplier condition) {
        ensureConfigurationIsMutable();
        requireRegisteredState(from);
        requireRegisteredState(to);
        Objects.requireNonNull(condition, "Transition condition cannot be null");

        transitions.get(from).add(Transition.when(to, condition));
        return this;
    }

    /**
     * Adds a transition that fires after the source state's command completes naturally.
     */
    public StateMachine<S> addTransition(S from, S to) {
        ensureConfigurationIsMutable();
        requireRegisteredState(from);
        requireRegisteredState(to);

        transitions.get(from).add(Transition.whenComplete(to));
        return this;
    }

    /**
     * Returns the active state, or {@code null} before the machine has started.
     */
    public S getCurrentState() {
        return currentState;
    }

    /**
     * Returns the state active immediately before the current one, or {@code null} when
     * no transition has occurred in this run.
     */
    public S getPreviousState() {
        return previousState;
    }

    @Override
    public Set<Object> requirements() {
        return Collections.unmodifiableSet(new LinkedHashSet<>(requirements));
    }

    @Override
    public int priority() {
        return states.values().stream().mapToInt(Command::priority).max().orElse(0);
    }

    @Override
    public InterruptedBehavior interruptedBehavior() {
        return InterruptedBehavior.END;
    }

    @Override
    public ConflictBehavior conflictBehavior() {
        return ConflictBehavior.OVERRIDE;
    }

    @Override
    public BlockedBehavior blockedBehavior() {
        return BlockedBehavior.CANCEL;
    }

    @Override
    public void start() {
        if (initialState == null) {
            throw new IllegalStateException("No initial state has been set");
        }

        configurationLocked = true;
        currentState = initialState;
        previousState = null;
        currentCommandEnded = false;
        complete = false;
        currentCommand().start();
    }

    @Override
    public boolean done() {
        return complete;
    }

    @Override
    public void execute() {
        if (complete || currentState == null) {
            return;
        }

        Command command = currentCommand();
        if (!currentCommandEnded) {
            command.execute();
            if (command.done()) {
                command.end(EndCondition.NATURALLY);
                currentCommandEnded = true;
            }
        }

        List<Transition<S>> outgoingTransitions = transitions.get(currentState);
        for (Transition<S> transition : outgoingTransitions) {
            if (transition.matches(currentCommandEnded)) {
                transitionTo(transition.destination);
                return;
            }
        }

        if (outgoingTransitions.isEmpty() && currentCommandEnded) {
            complete = true;
        }
    }

    @Override
    public void end(EndCondition endCondition) {
        Objects.requireNonNull(endCondition, "End condition cannot be null");

        if (currentState != null && !currentCommandEnded) {
            currentCommand().end(endCondition);
            // Ivy resumes suspended commands without calling start() again.
            if (endCondition != EndCondition.SUSPENDED) {
                currentCommandEnded = true;
            }
        }
    }

    private void transitionTo(S destination) {
        if (!currentCommandEnded) {
            currentCommand().end(EndCondition.INTERRUPTED);
        }

        previousState = currentState;
        currentState = destination;
        currentCommandEnded = false;
        currentCommand().start();
    }

    private Command currentCommand() {
        return states.get(currentState);
    }

    private void requireRegisteredState(S id) {
        Objects.requireNonNull(id, "State id cannot be null");
        if (!states.containsKey(id)) {
            throw new IllegalArgumentException("State does not exist: " + id);
        }
    }

    private void ensureConfigurationIsMutable() {
        if (configurationLocked) {
            throw new IllegalStateException("State machine configuration is locked after start");
        }
    }

    private static final class Transition<S> {
        private final S destination;
        private final BooleanSupplier condition;
        private final boolean onCompletion;

        private Transition(S destination, BooleanSupplier condition, boolean onCompletion) {
            this.destination = destination;
            this.condition = condition;
            this.onCompletion = onCompletion;
        }

        private static <S> Transition<S> when(S destination, BooleanSupplier condition) {
            return new Transition<>(destination, condition, false);
        }

        private static <S> Transition<S> whenComplete(S destination) {
            return new Transition<>(destination, null, true);
        }

        private boolean matches(boolean commandEnded) {
            return onCompletion ? commandEnded : condition.getAsBoolean();
        }
    }
}
