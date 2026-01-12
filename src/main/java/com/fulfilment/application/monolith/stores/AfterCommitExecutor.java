package com.fulfilment.application.monolith.stores;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Status;
import jakarta.transaction.Synchronization;
import jakarta.transaction.TransactionSynchronizationRegistry;

@ApplicationScoped
public class AfterCommitExecutor {

    @Inject TransactionSynchronizationRegistry txSyncRegistry;

    public void runAfterCommit(Runnable action) {
        if (action == null) {
            return;
        }

        try {
            int status = txSyncRegistry.getTransactionStatus();
            if (status == Status.STATUS_ACTIVE) {
                txSyncRegistry.registerInterposedSynchronization(
                        new Synchronization() {
                            @Override
                            public void beforeCompletion() {
                                // no-op
                            }

                            @Override
                            public void afterCompletion(int completionStatus) {
                                if (completionStatus == Status.STATUS_COMMITTED) {
                                    action.run();
                                }
                            }
                        });
                return;
            }
        } catch (IllegalStateException ignored) {
            // No transaction associated -> fall through to immediate execution
        }

        // If there is no active transaction, execute immediately.
        action.run();
    }
}
