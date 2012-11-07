package de.htw.ds.sync;

import java.util.Random;
import java.util.concurrent.Semaphore;
import de.htw.ds.TypeMetadata;


/**
 * <p>Demonstrates thread processing and thread re-synchronization using a runnable
 * in conjunction with semaphore signaling. </p>
 * <p>Especially note that semaphores do not require extra synchronization when compared to
 * monitors, as ticket release may happen before the main thread tries to acquire a ticket,
 * without provoking deadlocks!</p>
 */
@TypeMetadata(copyright="2008-2012 Sascha Baumeister, all rights reserved", version="0.2.2", authors="Sascha Baumeister")
public final class ResyncThreadBySemaphore {
	private static final int SECOND = 1000;
	private static final Random RANDOMIZER = new Random();


	/**
	 * Application entry point. The arguments must be a child thread count
	 * and the number of seconds the child threads should take for processing.
	 * @param args the arguments
	 * @throws IndexOutOfBoundsException if no thread count is passed
	 * @throws NumberFormatException if the given thread count is not an integral number
	 * @throws IllegalArgumentException if the given thread count is negative, or if
	 *    the given thread period is strictly negative
	 * @throws InterruptedException if a thread is interrupted while blocking
	 */
	public static void main(final String[] args) throws InterruptedException {
		final int threadCount = Integer.parseInt(args[0]);
		final int threadPeriod = Integer.parseInt(args[1]);
		resync(threadCount, threadPeriod);
	}


	/**
	 * Starts threadCount child threads and resynchronizes them, displaying the
	 * time it took for the longest running child to end.
	 * @throws IllegalArgumentException if the given thread count is negative, or if
	 *    the given thread period is strictly negative
	 * @throws InterruptedException if a child thread is interrupted while blocking
	 */
	private static void resync(final int threadCount, final int threadPeriod) throws InterruptedException {
		if (threadCount <= 0) throw new IllegalArgumentException();
		final long timestamp = System.currentTimeMillis();

		System.out.format("Starting %s Java thread(s)...\n", threadCount);
		final Semaphore indebtedSemaphore = new Semaphore(1 - threadCount);
		final Reference<Throwable> exceptionReference = new Reference<>();

		for (int index = 0; index < threadCount; ++index) {
			final Runnable runnable = new Runnable() {
				public void run() {
					try {
						final int sleepMillies = RANDOMIZER.nextInt(threadPeriod * SECOND);
						Thread.sleep(sleepMillies);
					} catch (final Throwable exception) {
						exceptionReference.put(exception);
					} finally {
						indebtedSemaphore.release();
					}
				}
			};

			new Thread(runnable).start();
		}

		System.out.println("Resynchronising Java thread(s)... ");
		indebtedSemaphore.acquireUninterruptibly();

		final Throwable exception = exceptionReference.get();
		if (exception != null) {
			if (exception instanceof Error) throw (Error) exception;
			if (exception instanceof RuntimeException) throw (RuntimeException) exception;
			if (exception instanceof InterruptedException) throw (InterruptedException) exception;
			throw new AssertionError();
		}
		System.out.format("Java thread(s) resynchronized after %sms.\n", System.currentTimeMillis() - timestamp);
	}
}