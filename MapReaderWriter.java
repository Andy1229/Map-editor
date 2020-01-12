import java.io.Reader;
import java.io.IOException;
import java.io.Writer;
import java.io.BufferedReader;
import java.io.FileWriter;

public class MapReaderWriter implements MapIo{
  //This class handles reading and writing map representations as 
  //described in the practical specification

  //Read the description of a map from the 
  //Reader r, and transfers it to Map, m.
    public void read(Reader r, Map m) throws IOException, MapFormatException{
        BufferedReader br = (BufferedReader)r;
        String line = br.readLine(); 
        int lineNum = 1; // line number
        // read line by line
        while(line!=null){
            try{
            	// split line by space.
                String[] fields = line.split(" ");
                if(fields[0].equals("place")){
                	// read place
                    String placeName = fields[1];
                    int xPos = Integer.parseInt(fields[2]);
                    int yPos = Integer.parseInt(fields[3]);
                    // add new place to map
                    m.newPlace(placeName, xPos, yPos);
                }else if(fields[0].equals("road")){
                	// read road
                    String firstPlaceName = fields[1];
                    String roadName = fields[2];
                    int length = Integer.parseInt(fields[3]);
                    String secondPlaceName = fields[4];
                    Place firstPlace = m.findPlace(firstPlaceName);
                    Place secondPlace = m.findPlace(secondPlaceName);
                    // add new road to map
                    m.newRoad(firstPlace, secondPlace, roadName, length);
                }else if(fields[0].equals("start")){
                	// read start place
                    String startPlaceName = fields[1];
                    Place startPlace = m.findPlace(startPlaceName);
                    // set start place for map
                    m.setStartPlace(startPlace);
                }else if(fields[0].equals("end")){
                	// read end place
                    String endPlaceName = fields[1];
                    Place endPlace = m.findPlace(endPlaceName);
                    // set end place for map
                    m.setEndPlace(endPlace);
                }
            }catch (IllegalArgumentException e){
                throw new MapFormatException(lineNum, e.getMessage());
            }catch (ArrayIndexOutOfBoundsException e){
                throw new MapFormatException(lineNum, e.getMessage());
            }
            line = br.readLine(); //read next line
            lineNum++; // increase line number
        }
        br.close(); //close reader
    }
    
    
    //Write a representation of the Map, m, to the Writer w.
    public void write(Writer w, Map m) throws IOException{
        FileWriter fw = (FileWriter) w;
        fw.write(m.toString());
        fw.close();
    }
}