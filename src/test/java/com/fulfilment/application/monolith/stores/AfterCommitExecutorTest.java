package com.fulfilment.application.monolith.stores;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.transaction.Status;
import jakarta.transaction.Synchronization;
import jakarta.transaction.TransactionSynchronizationRegistry;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;

class AfterCommitExecutorTest {

    @Test
    void runAfterCommitRegistersSynchronizationAndExecutesOnCommit() {
        FakeTransactionSynchronizationRegistry registry =
                new FakeTransactionSynchronizationRegistry(Status.STATUS_ACTIVE);
        AfterCommitExecutor executor = new AfterCommitExecutor();
        executor.txSyncRegistry = registry;

        AtomicBoolean ran = new AtomicBoolean(false);
        executor.runAfterCommit(() -> ran.set(true));

        assertFalse(ran.get());
        registry.fireAfterCompletion(Status.STATUS_COMMITTED);
        assertTrue(ran.get());
    }

    @Test
    void runAfterCommitDoesNotExecuteOnRollback() {
        FakeTransactionSynchronizationRegistry registry =
                new FakeTransactionSynchronizationRegistry(Status.STATUS_ACTIVE);
        AfterCommitExecutor executor = new AfterCommitExecutor();
        executor.txSyncRegistry = registry;

        AtomicBoolean ran = new AtomicBoolean(false);
        executor.runAfterCommit(() -> ran.set(true));

        registry.fireAfterCompletion(Status.STATUS_ROLLEDBACK);
        assertFalse(ran.get());
    }

    @Test
    void runAfterCommitExecutesImmediatelyWithoutActiveTransaction() {
        FakeTransactionSynchronizationRegistry registry =
                new FakeTransactionSynchronizationRegistry(Status.STATUS_NO_TRANSACTION);
        AfterCommitExecutor executor = new AfterCommitExecutor();
        executor.txSyncRegistry = registry;

        AtomicBoolean ran = new AtomicBoolean(false);
        executor.runAfterCommit(() -> ran.set(true));

        assertTrue(ran.get());
        assertNull(registry.getSynchronization());
    }

    @Test
    void runAfterCommitExecutesImmediatelyWhenRegistryThrows() {
        FakeTransactionSynchronizationRegistry registry =
                new FakeTransactionSynchronizationRegistry(Status.STATUS_ACTIVE);
        registry.setThrowIllegalState(true);

        AfterCommitExecutor executor = new AfterCommitExecutor();
        executor.txSyncRegistry = registry;

        AtomicBoolean ran = new AtomicBoolean(false);
        executor.runAfterCommit(() -> ran.set(true));

        assertTrue(ran.get());
    }

    @Test
    void runAfterCommitIgnoresNullAction() {
        FakeTransactionSynchronizationRegistry registry =
                new FakeTransactionSynchronizationRegistry(Status.STATUS_ACTIVE);
        AfterCommitExecutor executor = new AfterCommitExecutor();
        executor.txSyncRegistry = registry;

        executor.runAfterCommit(null);

        assertNull(registry.getSynchronization());
    }

    static class FakeTransactionSynchronizationRegistry implements TransactionSynchronizationRegistry {

        private final Map<Object, Object> resources = new HashMap<>();
        private final int status;
        private boolean rollbackOnly;
        private boolean throwIllegalState;
        private Synchronization synchronization;

        FakeTransactionSynchronizationRegistry(int status) {
            this.status = status;
        }

        void setThrowIllegalState(boolean throwIllegalState) {
            this.throwIllegalState = throwIllegalState;
        }

        Synchronization getSynchronization() {
            return synchronization;
        }

        void fireAfterCompletion(int completionStatus) {
            if (synchronization != null) {
                synchronization.afterCompletion(completionStatus);
            }
        }

        @Override
        public Object getTransactionKey() {
            return this;
        }

        @Override
        public void putResource(Object key, Object value) {
            resources.put(key, value);
        }

        @Override
        public Object getResource(Object key) {
            return resources.get(key);
        }

        @Override
        public void registerInterposedSynchronization(Synchronization synchronization) {
            this.synchronization = synchronization;
        }

        @Override
        public void setRollbackOnly() {
            rollbackOnly = true;
        }

        @Override
        public boolean getRollbackOnly() {
            return rollbackOnly;
        }

        @Override
        public int getTransactionStatus() {
            if (throwIllegalState) {
                throw new IllegalStateException("No transaction bound");
            }
            return status;
        }
    }
}
