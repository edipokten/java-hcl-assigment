package com.fulfilment.application.monolith.stores;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.transaction.Status;
import jakarta.transaction.Synchronization;
import jakarta.transaction.TransactionSynchronizationRegistry;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;

class AfterCommitExecutorTest {

  @Test
  void doesNothingWhenActionIsNull() {
    AfterCommitExecutor executor = new AfterCommitExecutor();
    executor.txSyncRegistry = new StubTransactionSynchronizationRegistry(Status.STATUS_ACTIVE);

    executor.runAfterCommit(null);

    assertFalse(((StubTransactionSynchronizationRegistry) executor.txSyncRegistry).registered);
  }

  @Test
  void registersSynchronizationWhenTransactionActive() {
    AfterCommitExecutor executor = new AfterCommitExecutor();
    StubTransactionSynchronizationRegistry registry =
        new StubTransactionSynchronizationRegistry(Status.STATUS_ACTIVE);
    executor.txSyncRegistry = registry;

    AtomicBoolean executed = new AtomicBoolean(false);
    executor.runAfterCommit(() -> executed.set(true));

    assertFalse(executed.get());
    assertTrue(registry.registered);

    registry.fireAfterCompletion(Status.STATUS_COMMITTED);
    assertTrue(executed.get());
  }

  @Test
  void executesImmediatelyWhenNoActiveTransaction() {
    AfterCommitExecutor executor = new AfterCommitExecutor();
    executor.txSyncRegistry = new StubTransactionSynchronizationRegistry(Status.STATUS_NO_TRANSACTION);

    AtomicBoolean executed = new AtomicBoolean(false);
    executor.runAfterCommit(() -> executed.set(true));

    assertTrue(executed.get());
  }

  @Test
  void executesImmediatelyWhenRegistryThrows() {
    AfterCommitExecutor executor = new AfterCommitExecutor();
    executor.txSyncRegistry = new ThrowingTransactionSynchronizationRegistry();

    AtomicBoolean executed = new AtomicBoolean(false);
    executor.runAfterCommit(() -> executed.set(true));

    assertTrue(executed.get());
  }

  private static final class StubTransactionSynchronizationRegistry
      implements TransactionSynchronizationRegistry {
    private final int status;
    private Synchronization synchronization;
    private boolean registered;

    private StubTransactionSynchronizationRegistry(int status) {
      this.status = status;
    }

    @Override
    public Object getResource(Object key) {
      return null;
    }

    @Override
    public void putResource(Object key, Object value) {
      // no-op
    }

    @Override
    public void registerInterposedSynchronization(Synchronization synchronization) {
      this.synchronization = synchronization;
      this.registered = true;
    }

    @Override
    public int getTransactionStatus() {
      return status;
    }

    @Override
    public Object getTransactionKey() {
      return null;
    }

    @Override
    public void setRollbackOnly() {
      // no-op
    }

    @Override
    public boolean getRollbackOnly() {
      return false;
    }

    private void fireAfterCompletion(int completionStatus) {
      if (synchronization != null) {
        synchronization.afterCompletion(completionStatus);
      }
    }
  }

  private static final class ThrowingTransactionSynchronizationRegistry
      implements TransactionSynchronizationRegistry {
    @Override
    public Object getResource(Object key) {
      return null;
    }

    @Override
    public void putResource(Object key, Object value) {
      // no-op
    }

    @Override
    public void registerInterposedSynchronization(Synchronization synchronization) {
      // no-op
    }

    @Override
    public int getTransactionStatus() {
      throw new IllegalStateException("No transaction");
    }

    @Override
    public Object getTransactionKey() {
      return null;
    }

    @Override
    public void setRollbackOnly() {
      // no-op
    }

    @Override
    public boolean getRollbackOnly() {
      return false;
    }
  }
}
