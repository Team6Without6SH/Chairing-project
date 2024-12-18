package com.sparta.chairingproject.config.transaction;

import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AfterCommitAction implements TransactionSynchronization {

	private final Runnable action;

	public static void register(Runnable action) {
		TransactionSynchronizationManager.registerSynchronization(new AfterCommitAction(action));
	}

	@Override
	public void afterCommit() {
		action.run();
	}
}
