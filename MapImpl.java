import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class MapImpl implements Map{
    private Set<Place> places = new HashSet<>();
    private Set<Road> roads = new HashSet<>();
    private Place startPlace;
    private Place endPlace;
    private Set<MapListener> mapListeners = new HashSet<>();
    //Add the MapListener ml to this map.
    //Note: A map can have multiple listeners
    public void addListener(MapListener ml){
      this.mapListeners.add(ml);
    }

    //Delete the MapListener ml from this map.
    public void deleteListener(MapListener ml){
      if(this.mapListeners.contains(ml)){
        this.mapListeners.remove(ml);
      }
    }

    //Create a new Place and add it to this map
    //Return the new place
    //Throws IllegalArgumentException if:
    //  the name is not valid or is the same as that
    //  of an existing place
    //Note: A valid placeName begins with a letter, and is 
    //followed by optional letters, digits, or underscore characters
    public Place newPlace(String placeName, int xPos, int yPos)
      throws IllegalArgumentException{
        Place place = new PlaceImpl(placeName, xPos, yPos);
        boolean isPlaceExist = false;
        for(Place p : this.places){
          if(p.getName().equals(placeName)){
            isPlaceExist = true;
            break;
          }
        }
        if(!Character.isAlphabetic(placeName.charAt(0))){
          // the name is not valid
          throw new IllegalArgumentException("invalid place name!");
        }else if(isPlaceExist){
          // existing place
          throw new IllegalArgumentException("place already exists!");
        }else{
          // add place to map
      		this.places.add(place);
          // notify all listeners
          for(MapListener mapListener : this.mapListeners){
            mapListener.placesChanged();
          }
          return place;
        }
      }

    //Remove a place from the map
    //If the place does not exist, returns without error
    public void deletePlace(Place s){
      if(this.places.contains(s)){
        // remove place if exists
        this.places.remove(s);
        // notify all listeners
        for(MapListener mapListener : this.mapListeners){
          mapListener.placesChanged();
        }
      }
    }

    //Find and return the Place with the given name
    //If no place exists with given name, return NULL
    public Place findPlace(String placeName){
      // go through all places
      for(Place p : this.places){
        // find place by name
        if(p.getName().equals(placeName)){
          return p;
        }
      }
      // return null if no place find
      return null;
    }


    //Return a set containing all the places in this map
    public Set<Place> getPlaces(){
      return this.places;
    }
    
    //Create a new Road and add it to this map
    //Returns the new road.
    //Throws IllegalArgumentException if:
    //  the firstPlace or secondPlace does not exist or
    //  the roadName is invalid or
    //  the length is negative
    //Note: A valid roadName is either the empty string, or starts
    //with a letter and is followed by optional letters and digits
    public Road newRoad(Place from, Place to, 
      String roadName, int length) 
      throws IllegalArgumentException{
        Road road = new RoadImpl(from, to, roadName, length);
        if(!this.places.contains(from) || !this.places.contains(to)){
          // firstPlace or secondPlace does not exist
          throw new IllegalArgumentException("place not exits!");
        }else if(!Character.isAlphabetic(roadName.charAt(0)) && !roadName.equals("-")){
          // roadName is invalid
          throw new IllegalArgumentException("invalid road name!");
        }else if(length < 0){
          // road length is negative
          throw new IllegalArgumentException("invalid road length!");
        }else{
          // add road to map
          this.roads.add(road);
          // set toRoad for place
          PlaceImpl toPlace = (PlaceImpl)to;
          PlaceImpl fromPlace = (PlaceImpl)from;
          toPlace.addToRoad(road);
          fromPlace.addToRoad(road);
          // notify all listeners
          for(MapListener mapListener : this.mapListeners){
            mapListener.roadsChanged();
          }
          return road;
        }
      }

    //Remove a road r from the map
    //If the road does not exist, returns without error
    public void deleteRoad(Road r){
      if(this.roads.contains(r)){
        // remove road if exists
        this.roads.remove(r);
        // notiry all listeners
        for(MapListener mapListener : this.mapListeners){
          mapListener.roadsChanged();
        }
      }
    }

    //Return a set containing all the roads in this map
    public Set<Road> getRoads(){
      return this.roads;
    }

    //Set the place p as the starting place
    //If p==null, unsets the starting place
    //Throws IllegalArgumentException if the place p is not in the map
    public void setStartPlace(Place p)
      throws IllegalArgumentException{
        if(p == null){
          if(this.startPlace != null){
            // unset startPlace
            PlaceImpl startPlace = (PlaceImpl) this.startPlace;
            startPlace.setIsStartPlace(false);
          }
          this.startPlace = null;
          for(MapListener mapListener : this.mapListeners){
            mapListener.placesChanged();
          }
        }else{
          if(this.places.contains(p)){
            if(this.startPlace != null){
                // unset startPlace
                PlaceImpl startPlace = (PlaceImpl) this.startPlace;
                startPlace.setIsStartPlace(false);
            }
              // set new start place
            PlaceImpl place = (PlaceImpl) p;
            place.setIsStartPlace(true);
            this.startPlace = place;
            // notify all listeners
            for(MapListener mapListener : this.mapListeners){
              mapListener.placesChanged();
            }
          }else{
            throw new IllegalArgumentException("place not in the map!");
          }
        }

        for(MapListener mapListener : this.mapListeners){
          mapListener.otherChanged();
        }
      }

    //Return the starting place of this map
    public Place getStartPlace(){
      return this.startPlace;
    }

    //Set the place p as the ending place
    //If p==null, unsets the ending place
    //Throws IllegalArgumentException if the place p is not in the map
    public void setEndPlace(Place p)
      throws IllegalArgumentException{
        if(p == null){
          if(this.endPlace != null){
              // unset end place is exists
              PlaceImpl endPlace = (PlaceImpl) this.endPlace;
              endPlace.setIsEndPlace(false);              
          }
          this.endPlace = null;
          for(MapListener mapListener : this.mapListeners){
            mapListener.placesChanged();
          }
        }else{
          if(this.places.contains(p)){
            if(this.endPlace != null){
              // unset end place is exists
              PlaceImpl endPlace = (PlaceImpl) this.endPlace;
              endPlace.setIsEndPlace(false);              
            }
            // set end place
            PlaceImpl place = (PlaceImpl) p;
            place.setIsEndPlace(true);
            this.endPlace = place;
            // notify all listeners
            for(MapListener mapListener : this.mapListeners){
              mapListener.placesChanged();
            }
          }else{
            throw new IllegalArgumentException("place not in the map!");
          }
        }

        for(MapListener mapListener : this.mapListeners){
          mapListener.otherChanged();
        }
     }

    //Return the ending place of this map
    public Place getEndPlace(){
      return this.endPlace;
    }

    //Causes the map to compute the shortest trip between the
    //"start" and "end" places
    //For each road on the shortest route, sets the "isChosen" property
    //to "true".
    //Returns the total distance of the trip.
    //Returns -1, if there is no route from start to end
    public int getTripDistance(){
      // use breadth-first traverse algorithm to find the shortest route
      for(Road road : this.roads){
        RoadImpl roadImpl = (RoadImpl) road;
        roadImpl.setChosen(false);
      }

      if(this.startPlace != null && this.endPlace != null){
        // define open list and close list
        List<PlaceImpl> openList = new ArrayList<>();
        List<Place> closeList = new ArrayList<>();
        // define hashmap to record current route
        HashMap<PlaceImpl, PlaceImpl> route = new HashMap<>();
        // define hashmap to record current accumulative cost
        HashMap<Place, Integer> accumulativeCost = new HashMap<>();

        // init cost
        for(Place place : this.places){
          accumulativeCost.put(place, Integer.MAX_VALUE);
        }

        // search start from endPlace to startPlace
        PlaceImpl endPlace = (PlaceImpl) this.endPlace;
        accumulativeCost.put(endPlace,0);
        // put endPlace to open list
        openList.add(endPlace);

        // stop search if open list is empty
        while(!openList.isEmpty()){
          // take out place from open list and put it into close list
          PlaceImpl currentPlace = (PlaceImpl) openList.remove(0);
          closeList.add(currentPlace);

          // record current accumulative cost
          int currentCost = accumulativeCost.get(currentPlace);

          // return if find start place
          if(currentPlace.isStartPlace()){
            // set "isChosen" property for road
            System.out.print("routes are : ");
            while(!currentPlace.isEndPlace()){
              PlaceImpl nextPlace = (PlaceImpl)route.get(currentPlace);
              RoadImpl road = (RoadImpl)currentPlace.roadTo(nextPlace);
              road.setChosen(true);
              currentPlace = nextPlace;

              // print route
              if(!currentPlace.isEndPlace()){
                System.out.print(road.toString() + "-->");
              }else{
                System.out.print(road.toString());
              }
            }
            System.out.println("\nthe shortest cost is " + currentCost);
            return currentCost;
          }

          // extends current place
          Set<Road> roads = currentPlace.toRoads();
          if(!roads.isEmpty()){
            // put next place into open list
            // update open list by cost
            for(Road road : roads){
              PlaceImpl nextPlace = null;
              if(currentPlace.equals(road.firstPlace())){
                nextPlace = (PlaceImpl)road.secondPlace();
              }else{
                nextPlace = (PlaceImpl)road.firstPlace();
              }
              if(!closeList.contains(nextPlace)){
                if(openList.contains(nextPlace)){
                int cost = currentCost + road.length();
                  if(cost < accumulativeCost.get(nextPlace)){
                    route.put(nextPlace,currentPlace);
                    accumulativeCost.put(nextPlace,cost);
                  }
                }else{
                  route.put(nextPlace, currentPlace);
                  accumulativeCost.put(nextPlace, currentCost + road.length());
                  openList.add(nextPlace);
                }
              }
            }
            // sort open list ascend by cost
            Collections.sort(openList, new Comparator<Place>(){
              @Override
              public int compare(Place p1, Place p2){
                return accumulativeCost.get(p1) - accumulativeCost.get(p2);
              }
            });
          }
        }
        // return -1 if no route from start to end
        // System.out.println("no route from start to end!");
        // System.out.println("the shortest distance is -1");
        return -1;
      }else{
        // System.out.println("no route from start to end!");
        return this.startPlace == null ? -2 : -3;
      }
    }

    //Return a string describing this map
    //Returns a string that contains (in this order):
    //for each place in the map, a line (terminated by \n)
    //  PLACE followed the toString result for that place
    //for each road in the map, a line (terminated by \n)
    //  ROAD followed the toString result for that road
    //if a starting place has been defined, a line containing
    //  START followed the name of the starting-place (terminated by \n)
    //if an ending place has been defined, a line containing
    //  END followed the name of the ending-place (terminated by \n)
    public String toString(){
      String result = "";
      for(Place place : this.places){
        result += "PLACE " + place.toString() + "\n";
      }
      for(Road road : this.roads){
        result += "ROAD " + road.toString() + "\n";
      }

      if(this.startPlace != null){
        result += "START " + startPlace.getName() + "\n";
      }
      if(this.endPlace != null){
        result += "END " + endPlace.getName() + "\n";
      }
      return result;
    }
}

// implement class for Place
class PlaceImpl implements Place{
  private String name;
  private int x;
  private int y;
  private boolean isStartPlace;
  private boolean isEndPlace;
  private Set<Road> toRoads = new HashSet<>();
  private Set<PlaceListener> placeListeners = new HashSet<>();

  // construct method
  public PlaceImpl(String name, int x, int y){
    this.name = name;
    this.x = x;
    this.y = y;
  }

    //Add the PlaceListener pl to this place. 
    //Note: A place can have multiple listeners
    public void addListener(PlaceListener pl){
        this.placeListeners.add(pl);
    }

    //Delete the PlaceListener pl from this place.
    public void deleteListener(PlaceListener pl){
        if(this.placeListeners.contains(pl)){
            this.placeListeners.remove(pl);
        }
    }

    // add road that reach this place
    public void addToRoad(Road r){
        this.toRoads.add(r);
    }

    //Return a set containing all roads that reach this place
    public Set<Road> toRoads(){
      Set<Road> result = new HashSet<>();
      for(Road road : this.toRoads){
        if(road != null){
          result.add(road);
        }
      }
      return result;
    }

    //Return the road from this place to dest, if it exists
    //Returns null, if it does not
    public Road roadTo(Place dest){
        Set<Road> roads = dest.toRoads();
        if(!roads.isEmpty()){
          for(Road road : roads){
            if(road.firstPlace().equals(this) || road.secondPlace().equals(this)){
              return road;
            }
          }
        }
      return null;
    }
    

    //Move the position of this place 
    //by (dx,dy) from its current position
    public void moveBy(int dx, int dy){
      this.x = this.x + dx;
      this.y = this.y + dy;
      for(PlaceListener listener : this.placeListeners){
        listener.placeChanged();
      }
      for(Road road : this.toRoads){
        RoadImpl roadImpl = (RoadImpl) road;
        for(RoadListener roadListener : roadImpl.roadListeners){
          roadListener.roadChanged();
        }
      }
    }
    
    //Return the name of this place 
    public String getName(){
      return this.name;
    }

    //Return the X position of this place
    public int getX(){
      return this.x;
    }
    
    //Return the Y position of this place
    public int getY(){
      return this.y;
    }

    // set isStartPlace
    public void setIsStartPlace(boolean isStartPlace){
        this.isStartPlace = isStartPlace;
        for(PlaceListener listener : this.placeListeners){
          listener.placeChanged();
        }
    }

    //Return true if this place is the starting place for a trip
    public boolean isStartPlace(){
      return this.isStartPlace;
    }

    // set isEndPlace
    public void setIsEndPlace(boolean isEndPlace){
        this.isEndPlace = isEndPlace;
        for(PlaceListener listener : this.placeListeners){
          listener.placeChanged();
        }
    }
    //Return true if this place is the ending place for a trip
    public boolean isEndPlace(){
      return this.isEndPlace;
    }

    //Return a string containing information about this place 
    //in the form (without the quotes, of course!) :
    //"placeName(xPos,yPos)"  
    public String toString(){
      String result = this.name + "(" + this.x + "," + this.y + ")";
      return result;
    } 
}

class RoadImpl implements Road{
  private String name;
  private Place firstPlace;
  private Place secondPlace;
  private int length;
  private boolean isChosen;
  public Set<RoadListener> roadListeners = new HashSet<>();

    // construct method
  public RoadImpl(Place from, Place to, String name, int length){
    this.name = name;
    this.firstPlace = from;
    this.secondPlace = to;
    this.length = length;
  }

  //Add the RoadListener rl to this place.
    //Note: A road can have multiple listeners
    public void addListener(RoadListener rl){
        this.roadListeners.add(rl);
    }


    //Delete the RoadListener rl from this place.
    public void deleteListener(RoadListener rl){
        if(this.roadListeners.contains(rl)){
            this.roadListeners.remove(rl);
        }
    }

    //Return the first place of this road
    //Note: The first place of a road is the place whose name
    //comes EARLIER in the alphabet.
    public Place firstPlace(){
      return this.firstPlace;
    }
    
    //Return the second place of this road
    //Note: The second place of a road is the place whose name
    //comes LATER in the alphabet.
    public Place secondPlace(){
      return this.secondPlace;
    }
    
    // set isChosen property
    public void setChosen(boolean isChosen){
        this.isChosen = isChosen;
        for(RoadListener roadListener : this.roadListeners){
            roadListener.roadChanged();
        }
    }

    //Return true if this road is chosen as part of the current trip
    public boolean isChosen(){
      return this.isChosen;
    }

    //Return the name of this road
    public String roadName(){
      return this.name;
    }


    //Return the length of this road
    public int length(){
      return this.length;
    }

    //Return a string containing information about this road 
    //in the form (without quotes, of course!):
    //"firstPlace(roadName:length)secondPlace"
    public String toString(){
      String result = this.firstPlace.getName() + "(" + this.name + ":" + this.length + ")" + this.secondPlace.getName();
      return result;
    }
}