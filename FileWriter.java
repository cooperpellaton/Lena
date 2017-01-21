public class FileWriter {

    public List<Pictures> readFromFile(String filePath) {
	try {
	    List<Pictures> toReturn = new List<Pictures>();
	    FileInputStream is = new FileInputStream(filePath);
	    XMLDecoder decoder = new XMLDecoder(is);
	    // I'm not actually sure of the output of readObject. I am gathering
	    // that it will be okay for me to attempt to put it in an array and
	    // then read the contents out, but you might need to tweak this.
	    Object[] object = (Object)decoder.readObject();
	    for (Object x: object) {
		toReturn.add(x);
	    }
	    decoder.close();
	    return toReturn;
	} catch (Exception E) {
	    throw new Exception("An error occurred!");
	}
    }

    public boolean writeToFile(List<Pictures> toWrite, String filePath) {
	try {
	    FileOutputStream os =new FileOutputStream(filePath);
	    XMLEncoder encoder=new XMLEncoder(os);
	    Object[] pictureRepresentation = toWrite.toArray();
	    encoder.writeObject(pictureRepresentation);
	    encoder.close();
	} catch (Exception E) {
	    throw new Exception("There was an error that occured while writing to the file!");
	}
    }
}
