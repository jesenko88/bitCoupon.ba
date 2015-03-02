package helpers;

import org.mindrot.jbcrypt.BCrypt;

public class HashHelper {
	
	public static boolean checkPass(String candidate, String encryptedPassword){
		
		if (candidate == null){
			return false;
		}
		if (encryptedPassword == null){
			return false;
		}
		return BCrypt.checkpw(candidate, encryptedPassword);
	}
	
	
	public static String createPassword(String clearString)throws IllegalArgumentException {



			if (clearString == null) {
				throw new IllegalArgumentException("empty.password");

			}
			return BCrypt.hashpw(clearString, BCrypt.gensalt());

			}

}
