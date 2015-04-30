package helpers;

import org.mindrot.jbcrypt.BCrypt;

import play.Logger;

public class HashHelper {
	
	/**
	 * Checks if the entered password is correct
	 * @param candidate - entered password
	 * @param encryptedPassword - user password
	 * @return true if the entered password is correct, else return false
	 */
	public static boolean checkPass(String candidate, String encryptedPassword){
		
		if (candidate == null){
			return false;
		}
		if (encryptedPassword == null){
			return false;
		}
		return BCrypt.checkpw(candidate, encryptedPassword);
	}
	
	
	/**
	 * Create hashed password
	 * @param clearString - user password
	 * @return hashed password
	 * @throws IllegalArgumentException
	 */
	public static String createPassword(String clearString)throws IllegalArgumentException {

			if (clearString == null) {
				Logger.error("empty.password");
				throw new IllegalArgumentException("empty.password");

			}
			return BCrypt.hashpw(clearString, BCrypt.gensalt());

			}

}
