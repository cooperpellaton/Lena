import java.secruity.*;

public class Hash {
    public string hashFile(String fileName){
	try { 
	    MessageDigest md = MessageDigest.getInstance("MD5");
	    md.reset();
	    md.update(fileName.getBytes());
	    byte[] digest = m.digest();
	    BigInteger bigInt = new BigInteger(1,digest);
	    String hashtext = bigInt.toString(16);
	    while(hashtext.length() < 32 ) {
		hashtext = "0" + hashtext;
	    }
	    return hastext;
	} catch (java.security.NoSuchAlgorithmException e) {}
	return null;
    }
}
