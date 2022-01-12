package br.ufsc.mov3lets.utils.log;

/**
 * Ascii progress meter. On completion this will reset itself,
 * so it can be reused
 * <br /><br />
 * 100% ################################################## |
 */
public interface ProgressBar {

	/**
	 * Plus.
	 */
	void plus();

	/**
	 * Plus.
	 *
	 * @param message the message
	 */
	void plus(String message);

	/**
	 * Plus.
	 *
	 * @param size the size
	 */
	void plus(long size);

	/**
	 * Plus.
	 *
	 * @param size the size
	 * @param message the message
	 */
	void plus(long size, String message);

	/**
	 * Update.
	 *
	 * @param done the done
	 * @param total the total
	 */
	void update(long done, long total);

	/**
	 * called whenever the progress bar needs to be updated.
	 * that is whenever progress was made.
	 *
	 * @param done an int representing the work done so far
	 * @param total an int representing the total work
	 * @param message the message
	 */
	void update(long done, long total, String message);

	/**
	 * Trace.
	 *
	 * @param message the message
	 */
	void trace(String message);

	/**
	 * Sets the inline.
	 *
	 * @param inline the new inline
	 */
	void setInline(boolean inline);

	/**
	 * Sets the prefix.
	 *
	 * @param prefix the new prefix
	 */
	void setPrefix(String prefix);

	/**
	 * Sets the total.
	 *
	 * @param total the new total
	 */
	void setTotal(long total);

	void reset(long total);

}