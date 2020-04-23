package net.jitse.npclib;

/**
 * Mutable preferences for library usage
 * 
 * @author A248
 *
 */
public class NPCLibOptions {

	MovementHandling moveHandling;

	/**
	 * Creates the default options
	 * 
	 */
	public NPCLibOptions() {
		moveHandling = MovementHandling.playerMoveEvent();
	}

	/**
	 * Specifies the motion handling which will be used for the library. <br>
	 * Programmers may choose between using the PlayerMoveEvent or
	 * a periodic task. <br>
	 * <br>
	 * Note that NPCLib will always use events such as the PlayerTeleportEvent
	 * and PlayerChangedWorldEvent in addition to the specified option.
	 * 
	 * @param moveHandling the movement handling
	 * @return the same NPCLibOptions
	 */
	public NPCLibOptions setMovementHandling(MovementHandling moveHandling) {
		this.moveHandling = moveHandling;
		return this;
	}

	/**
	 * Options relating to movement handling
	 * 
	 * @author A248
	 *
	 */
	public static class MovementHandling {

		final boolean usePme;
		final long updateInterval;

		private MovementHandling(boolean usePme, long updateInterval) {
			this.usePme = usePme;
			this.updateInterval = updateInterval;
		}

		/**
		 * Gets movement handling using the PlayerMoveEvent
		 * 
		 * @return movement handling
		 */
		public static MovementHandling playerMoveEvent() {
			return new MovementHandling(false, 0);
		}

		/**
		 * Gets movement handling using a periodic update interval in ticks.
		 * 
		 * @param updateInterval the update interval in ticks
		 * @return movement handling
		 */
		public static MovementHandling repeatingTask(long updateInterval) {
			if (updateInterval <= 0) {
				throw new IllegalArgumentException("Negative update interval");
			}
			return new MovementHandling(true, updateInterval);
		}

	}

}
