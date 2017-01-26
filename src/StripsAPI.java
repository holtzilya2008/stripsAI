import java.util.ArrayList;

public class StripsAPI {
/* -------------------------------- fields *-------------------------------- */
	
	private GameGraphics game;

/* ---------------------------- Constant Values ---------------------------- */

	// Rooms definition:  in the format RecInfo(x1,x2,y1,y2)
	public final RecInfo ROOM1 = new RecInfo(0,7,0,4);
	public final RecInfo ROOM2 = new RecInfo(0,7,5,11);
	public final RecInfo ROOM3 = new RecInfo(8,19,0,11);
	public final RecInfo DOORWAY_ROOMS12 = new RecInfo(2,5,4,5);
	public final RecInfo DOORWAY_ROOMS13 = new RecInfo(7,8,1,3);
	public final RecInfo DOORWAY_ROOMS23 = new RecInfo(7,8,6,10);
	
	
/* ---------------------------- DEBUG Environment -------------------------- */

	private static final String DEBUG_TAG = "StripsAPI";
	private static final int DEBUG_ALL = 0;
	private static final int DEBUG_CLASS = 1;
	private static final int DEBUG_FUNCTION = 2;
	private static final int DEBUG_SPECIFIC = 3;
	private static final int CURRENT_DEBUG_LEVEL = DEBUG_SPECIFIC;

/* ---------------------------- Object Construction ------------------------ */

	public StripsAPI(GameGraphics game){
		debugPrint(DEBUG_CLASS,"In constructor");
		this.game = game;
	}

/* ----------------------------- Public Methods ---------------------------- */

	/* ------ Auxiliary methods -------*/
	
	/**
	 * 
	 * @param the current furniture
	 * @return one of the Rooms or dorway defined above 
	 * under Rooms definition
	 */
	public RecInfo getRoom(RecInfo rect){
		if(rect.getX2() <= this.ROOM1.getX2()){
			if(rect.getY2() <= this.ROOM1.getY2()){
				return this.ROOM1;
			}
			return this.ROOM2;
		}
		return this.ROOM3;
	}

	/**
	 * Method returns the obstacle that prevets "rect" to allpy the action
	 * @param rect - the rectangle we want to move
	 * @param actionName - the name of the action we want to preform
	 * @return - Obstacle info
	 */
	public RecInfo getObstacle(RecInfo rect,String actionName){
		RecInfo info = null;
		switch(actionName){
			case Action.MOVE_LEFT:		
				return game.getRectangleByYAxis(rect.getX1()-1, rect.getY1(), rect.getY2());
			case Action.MOVE_RIGHT:
				return game.getRectangleByYAxis(rect.getX2()+1, rect.getY1(), rect.getY2());
			case Action.MOVE_UP:
				return game.getRectangleByXAxis(rect.getX1(), rect.getX2(), rect.getY1()-1);
			case Action.MOVE_DOWN:
				return game.getRectangleByXAxis(rect.getX1(), rect.getX2(), rect.getY2()+1);
			case Action.ROTATE_LEFT:
				for(int i = rect.getX1()-1-rect.getEdge2(); i <= rect.getX1()-1; i++){
					info = game.getRectangleByYAxis(i, rect.getY2()-rect.getEdge1(), rect.getY2());
					if(info != null){
						return info;
					}
				}
				debugPrint(DEBUG_SPECIFIC,"Null Pointer Received");
				return null;
			case Action.ROTATE_RIGHT:
				for(int i = rect.getX1(); i <= rect.getX1()+rect.getEdge2(); i++){
					info = game.getRectangleByYAxis(i, rect.getY2()-1, rect.getY2()+rect.getEdge1()-1);
					if(info != null){
						return info;
					}
				}
				debugPrint(DEBUG_SPECIFIC,"Null Pointer Received");
				return null;	
			default: 
				debugPrint(DEBUG_SPECIFIC,"Null Pointer Received");
				return null;		
		}
	}

	/**
	 * Method calculates temprary place, in the same room, where we can 
	 * move the Obstacle, so we will be able to preform the action
	 */
	public RecInfo findTempObstaclePlace(RecInfo rect, 
									     RecInfo obstacle,
									     String actionName){
		RecInfo currentRoom = getRoom(rect);
		int distanceToWall;
		
		int theMostRightPick;
		int theMostLeftPick;
		int theLowestPick;
		int theHighestPick;
		
		int distanceToMoveLeft;
		int distanceToMoveDown;
		
		RecInfo tempRec = null;
		
		boolean found = false;
		int counter;
			switch(actionName){
			case Action.MOVE_LEFT:		
				distanceToWall = currentRoom.getY2() - rect.getY2();
				theLowestPick = obstacle.getY2();
				if (distanceToWall >= obstacle.getEdge2()+1){
					for(int i = obstacle.getY2()+1; i <= rect.getY2()+obstacle.getEdge2()+1; i++){
						if (game.getRectangleByXAxis(obstacle.getX1(), obstacle.getX2(), i) != null){
							theLowestPick = i -1;
							found = true;
							break;
						}
					}
					if (!found){
						return new RecInfo(obstacle.getX1(), obstacle.getX2(), 
								rect.getY2() + 1, rect.getY2()+obstacle.getEdge2()+1);
					}
					found = false;	
				}
				else{
					theLowestPick = rect.getY2()+distanceToWall;
					for(int i = obstacle.getY2()+1; i <= rect.getY2()+distanceToWall; i++){
						if (game.getRectangleByYAxis(i, obstacle.getY1(), obstacle.getY2()) != null){
							theLowestPick = i - 1;
							break;
						}
					}
				}
				distanceToWall = rect.getY1() - currentRoom.getY1();
				theHighestPick = obstacle.getY1();
				if (distanceToWall >= obstacle.getEdge2()+1){
					for(int i = obstacle.getY1()-1; i >= rect.getY1()-obstacle.getEdge2()-1; i--){
						if (game.getRectangleByXAxis(obstacle.getX1(), obstacle.getX2(), i) != null){
							theHighestPick = i + 1;
							found = true;
							break;
						}
					}
					if (!found){
						return new RecInfo(obstacle.getX1(), obstacle.getX2(), 
								rect.getY1() - 1, rect.getY1()-obstacle.getEdge2()-1);
					}
					found = false;	
				}
				else{
					theHighestPick = rect.getY1()-distanceToWall;
					for(int i = obstacle.getY1()-1; i <= rect.getY1()-distanceToWall; i--){
						if (game.getRectangleByYAxis(i, obstacle.getY1(), obstacle.getY2()) != null){
							theHighestPick = i + 1;
							break;
						}
					}
				}
				tempRec = game.getRectangleByYAxis(obstacle.getX1()-1, theLowestPick, theHighestPick);
				while (tempRec != null){
					theLowestPick = tempRec.getY2()+1;
					tempRec = game.getRectangleByYAxis(obstacle.getX1()-1, theLowestPick, theHighestPick);
				}
				if(theHighestPick+obstacle.getEdge2() <= theLowestPick){
					return new RecInfo(obstacle.getX1()-1, obstacle.getX2()-1, theLowestPick, theLowestPick+obstacle.getEdge2());
				}
				return null;
			case Action.MOVE_RIGHT:
				distanceToWall = currentRoom.getY2() - rect.getY2();
				theLowestPick = obstacle.getY2();
				if (distanceToWall >= obstacle.getEdge2()+1){
					for(int i = obstacle.getY2()+1; i <= rect.getY2()+obstacle.getEdge2()+1; i++){
						if (game.getRectangleByXAxis(obstacle.getX1(), obstacle.getX2(), i) != null){
							theLowestPick = i -1;
							found = true;
							break;
						}
					}
					if (!found){
						return new RecInfo(obstacle.getX1(), obstacle.getX2(), 
								rect.getY2() + 1, rect.getY2()+obstacle.getEdge2()+1);
					}
					found = false;	
				}
				else{
					theLowestPick = rect.getY2()+distanceToWall;
					for(int i = obstacle.getY2()+1; i <= rect.getY2()+distanceToWall; i++){
						if (game.getRectangleByYAxis(i, obstacle.getY1(), obstacle.getY2()) != null){
							theLowestPick = i - 1;
							break;
						}
					}
				}
				distanceToWall = rect.getY1() - currentRoom.getY1();
				theHighestPick = obstacle.getY1();
				if (distanceToWall >= obstacle.getEdge2()+1){
					for(int i = obstacle.getY1()-1; i >= rect.getY1()-obstacle.getEdge2()-1; i--){
						if (game.getRectangleByXAxis(obstacle.getX1(), obstacle.getX2(), i) != null){
							theHighestPick = i + 1;
							found = true;
							break;
						}
					}
					if (!found){
						return new RecInfo(obstacle.getX1(), obstacle.getX2(), 
								rect.getY1() - 1, rect.getY1()-obstacle.getEdge2()-1);
					}
					found = false;	
				}
				else{
					theHighestPick = rect.getY1()-distanceToWall;
					for(int i = obstacle.getY1()-1; i <= rect.getY1()-distanceToWall; i--){
						if (game.getRectangleByYAxis(i, obstacle.getY1(), obstacle.getY2()) != null){
							theHighestPick = i + 1;
							break;
						}
					}
				}
				tempRec = game.getRectangleByYAxis(obstacle.getX2()+1, theLowestPick, theHighestPick);
				while (tempRec != null){
					theLowestPick = tempRec.getY2()+1;
					tempRec = game.getRectangleByYAxis(obstacle.getX2()+1, theLowestPick, theHighestPick);
				}
				if(theHighestPick+obstacle.getEdge2() <= theLowestPick){
					return new RecInfo(obstacle.getX1()+1, obstacle.getX2()+1, theLowestPick, theLowestPick+obstacle.getEdge2());
				}
				return null;
			case Action.MOVE_UP:
				distanceToWall = currentRoom.getX2() - rect.getX2();
				theMostRightPick = obstacle.getX2();
				if (distanceToWall >= obstacle.getEdge1()+1){
					for(int i = obstacle.getX2()+1; i <= rect.getX2()+obstacle.getEdge1()+1; i++){
						if (game.getRectangleByYAxis(i, obstacle.getY1(), obstacle.getY2()) != null){
							theMostRightPick = i -1;
							found = true;
							break;
						}
					}
					if (!found){
						return new RecInfo(rect.getX2()+1, rect.getX2()+obstacle.getEdge1()+1, 
								obstacle.getY1(), obstacle.getY2());
					}
					found = false;	
				}
				else{
					theMostRightPick = rect.getX2()+distanceToWall;
					for(int i = obstacle.getX2()+1; i <= rect.getX2()+distanceToWall; i++){
						if (game.getRectangleByYAxis(i, obstacle.getY1(), obstacle.getY2()) != null){
							theMostRightPick = i - 1;
							break;
						}
					}
				}
				distanceToWall = rect.getX1() - currentRoom.getX1();
				theMostLeftPick = obstacle.getX1();
				if (distanceToWall >= obstacle.getEdge1()+1){
					for(int i = obstacle.getX1()-1; i >= rect.getX1()-obstacle.getEdge1()-1; i--){
						if (game.getRectangleByYAxis(i, obstacle.getY1(), obstacle.getY2()) != null){
							theMostLeftPick = i + 1;
							found = true;
							break;
						}
					}
					if (!found){
						return new RecInfo(obstacle.getX1(), obstacle.getX2(), 
								rect.getY1() - 1, rect.getY1()-obstacle.getEdge2()-1);
					}
					found = false;	
				}
				else{
					theMostLeftPick = rect.getX1()-distanceToWall;
					for(int i = obstacle.getX1()-1; i >= rect.getX1()-obstacle.getEdge1()-1; i--){
						if (game.getRectangleByYAxis(i, obstacle.getY1(), obstacle.getY2()) != null){
							theMostLeftPick = i + 1;
							break;
						}
					}
				}
				tempRec = game.getRectangleByXAxis(theMostLeftPick, theMostRightPick, obstacle.getY1()-1);
				while (tempRec != null){
					theMostLeftPick = tempRec.getX2()+1;
					tempRec = game.getRectangleByXAxis(theMostLeftPick, theMostRightPick, obstacle.getY1()-1);
				}
				if(theMostLeftPick+obstacle.getEdge1() <= theMostRightPick){
					return new RecInfo(theMostLeftPick,theMostLeftPick+obstacle.getEdge1(), obstacle.getY1()-1, obstacle.getY2()-1);
				}
				return null;
			case Action.MOVE_DOWN:
				distanceToWall = currentRoom.getX2() - rect.getX2();
				theMostRightPick = obstacle.getX2();
				if (distanceToWall >= obstacle.getEdge1()+1){
					for(int i = obstacle.getX2()+1; i <= rect.getX2()+obstacle.getEdge1()+1; i++){
						if (game.getRectangleByYAxis(i, obstacle.getY1(), obstacle.getY2()) != null){
							theMostRightPick = i -1;
							found = true;
							break;
						}
					}
					if (!found){
						return new RecInfo(rect.getX2()+1, rect.getX2()+obstacle.getEdge1()+1, 
								obstacle.getY1(), obstacle.getY2());
					}
					found = false;	
				}
				else{
					theMostRightPick = rect.getX2()+distanceToWall;
					for(int i = obstacle.getX2()+1; i <= rect.getX2()+distanceToWall; i++){
						if (game.getRectangleByYAxis(i, obstacle.getY1(), obstacle.getY2()) != null){
							theMostRightPick = i - 1;
							break;
						}
					}
				}
				distanceToWall = rect.getX1() - currentRoom.getX1();
				theMostLeftPick = obstacle.getX1();
				if (distanceToWall >= obstacle.getEdge1()+1){
					for(int i = obstacle.getX1()-1; i >= rect.getX1()-obstacle.getEdge1()-1; i--){
						if (game.getRectangleByYAxis(i, obstacle.getY1(), obstacle.getY2()) != null){
							theMostLeftPick = i + 1;
							found = true;
							break;
						}
					}
					if (!found){
						return new RecInfo(obstacle.getX1(), obstacle.getX2(), 
								rect.getY1() - 1, rect.getY1()-obstacle.getEdge2()-1);
					}
					found = false;	
				}
				else{
					theMostLeftPick = rect.getX1()-distanceToWall;
					for(int i = obstacle.getX1()-1; i > rect.getX1()-obstacle.getEdge1(); i--){
						if (game.getRectangleByYAxis(i, obstacle.getY1(), obstacle.getY2()) != null){
							theMostLeftPick = i + 1;
							break;
						}
					}
				}
				tempRec = game.getRectangleByXAxis(theMostLeftPick, theMostRightPick, obstacle.getY2()+1);
				while (tempRec != null){
					theMostLeftPick = tempRec.getX2()+1;
					tempRec = game.getRectangleByXAxis(theMostLeftPick, theMostRightPick, obstacle.getY2()+1);
				}
				if(theMostLeftPick+obstacle.getEdge1() <= theMostRightPick){
					return new RecInfo(theMostLeftPick,theMostLeftPick+obstacle.getEdge1(), obstacle.getY1()+1, obstacle.getY2()+1);
				}
				return null;
			case Action.ROTATE_LEFT://TODO calculate the exact upper limit
				distanceToWall = currentRoom.getY2() - rect.getY2();
				theLowestPick = obstacle.getY2();
				if (distanceToWall >= obstacle.getEdge2()+1){
					for(int i = obstacle.getY2()+1; i < rect.getY2()+obstacle.getEdge2(); i++){
						if (game.getRectangleByXAxis(obstacle.getX1(), obstacle.getX2(), i) != null){
							theLowestPick = i -1;
							found = true;
							break;
						}
					}
					if (!found){
						return new RecInfo(obstacle.getX1(), obstacle.getX2(), 
								rect.getY2() + 1, rect.getY2()+obstacle.getEdge2()+1);
					}
					found = false;	
				}
				distanceToWall = rect.getY1() - currentRoom.getY1();
				theHighestPick = obstacle.getY1();
				if (distanceToWall >= obstacle.getEdge2()+1){
					for(int i = obstacle.getY1()-1; i > rect.getY1()-obstacle.getEdge2(); i--){
						if (game.getRectangleByXAxis(obstacle.getX1(), obstacle.getX2(), i) != null){
							theHighestPick = i + 1;
							found = true;
							break;
						}
					}
					if (!found){
						return new RecInfo(obstacle.getX1(), obstacle.getX2(), 
								rect.getY1() - 1, rect.getY1()-obstacle.getEdge2()-1);
					}
					found = false;	
				}
				distanceToMoveLeft = obstacle.getX1() - (rect.getX1()-1-rect.getEdge2());
				counter = -1;
				for(int i = theHighestPick; i <= theLowestPick - obstacle.getEdge2(); i--){
					if (game.getRectangleByXAxis(obstacle.getX1()-distanceToMoveLeft, 
							obstacle.getX2()-distanceToMoveLeft, i) != null){
						counter = -1;
					}
					else{
						counter++;
						if (counter == obstacle.getEdge2()){
							return new RecInfo(obstacle.getX1()-distanceToMoveLeft, 
									obstacle.getX2()-distanceToMoveLeft, 
									i, i+obstacle.getEdge2());
						}
					}
				}
				return null;
			case Action.ROTATE_RIGHT:
				distanceToWall = currentRoom.getX2() - (rect.getX1()+rect.getEdge2());
				theMostRightPick = obstacle.getY2();
				if (distanceToWall >= obstacle.getEdge1()+1){
					for(int i = obstacle.getX2()+1; i < rect.getY1()+rect.getEdge2()+obstacle.getEdge2(); i++){
						if (game.getRectangleByYAxis(i, obstacle.getY1(), obstacle.getY2()) != null){
							theMostRightPick = i -1;
							found = true;
							break;
						}
					}
					if (!found){
						return new RecInfo(rect.getX1()+rect.getEdge2()+1, 
								rect.getX1()+rect.getEdge2()+obstacle.getEdge1(), 
								rect.getY1(), rect.getY2());
					}
					found = false;	
				}
				distanceToWall = rect.getX1() - currentRoom.getX1();
				theMostLeftPick = obstacle.getY1();
				if (distanceToWall >= obstacle.getEdge1()+1){
					for(int i = obstacle.getX1()-1; i > rect.getX1()-obstacle.getEdge1(); i--){
						if (game.getRectangleByYAxis(i, obstacle.getY1(), obstacle.getY2()) != null){
							theMostLeftPick = i + 1;
							found = true;
							break;
						}
					}
					if (!found){
						return new RecInfo(rect.getX1()-obstacle.getEdge1()-1, 
								rect.getX1()-1, rect.getY1(), rect.getY2());
					}
					found = false;	
				}
				distanceToMoveDown = rect.getX2() - (obstacle.getX1()-1);
				counter = -1;
				for(int i = theMostRightPick; i <= theMostLeftPick - obstacle.getEdge1(); i--){
					if (game.getRectangleByYAxis(i, obstacle.getY1()+distanceToMoveDown, 
							obstacle.getY2()+distanceToMoveDown) != null){
						counter = -1;
					}
					else{
						counter++;
						if (counter == obstacle.getEdge2()){
							return new RecInfo(i-obstacle.getEdge2(), i, obstacle.getY1()+distanceToMoveDown,
									obstacle.getY2()+distanceToMoveDown);
						}
					}
				}
				return null;
			default: 
				return null;		
		} //Rotations done only for the case obstacle is on the rotate destination area
	}

	/**
	 * Method returns aמ appropriate spot near the doorway between the 
	 * the currentRoom and the targedRoom, INSIDE THE CURRENT ROOM!
	 * e.g : if we have 
	 * 			furniture(0,2,0,1)
	 *			currentRoom(0,7,0,4)
	 * 			targedRoom(8,19,5,11)
	 * 		Then the appropriate spot near the doorway will be: 
	 * 			doorWay(5,7,1,2)
	 */ 
	
	private RecInfo findSpotNearVerticalDoorway(RecInfo door, int edge, int x1, int x2){
		int counter = -1;
		for (int i = door.getY1(); i <= door.getY2(); i++){
			if (game.getRectangleByXAxis(x1, x2, i) != null){
				counter = -1;
			}
			else{
				counter ++;
				if(counter == edge){
					return new RecInfo(x1,x2, i-edge, i);
				}
			}
		}
		return null;
	}
	
	
	private RecInfo findSpotNearHorizontalDoorway(RecInfo door, int edge, int y1, int y2){
		int counter = -1;
		for (int i = door.getX1(); i <= door.getX2(); i++){
			if (game.getRectangleByYAxis(i, y1, y2) != null){
				counter = -1;
			}
			else{
				counter++;
				if(counter == edge){
					return new RecInfo(i-edge, i, y1,y2);
				}
			}
		}
		return null;
	}
	
/*	public static final RecInfo DOORWAY_ROOMS12 = new RecInfo(2,5,4,5);
	public static final RecInfo DOORWAY_ROOMS13 = new RecInfo(7,8,1,3);
	public static final RecInfo DOORWAY_ROOMS23 = new RecInfo(7,8,6,10);*/
	public RecInfo findSpotNearDorway(RecInfo furniture, RecInfo currentRoom,
												RecInfo targetRoom){//TO ADD CHECK ALONG THE WALL
		RecInfo door;
		int x1, x2, y1, y2;
		int counter;
		if(currentRoom == this.ROOM1 && targetRoom == this.ROOM3){
			door = this.DOORWAY_ROOMS13;
			x1 = door.getX1() - furniture.getEdge1() - 1;
			x2 = door.getX1() - 1;
			return findSpotNearVerticalDoorway(door, furniture.getEdge2(), x1, x2);
		}
		
		if(currentRoom == this.ROOM1 && targetRoom == this.ROOM2){
			door = this.DOORWAY_ROOMS12;
			y1 = door.getY1() - furniture.getEdge2() - 1;
			y2 = door.getY1() - 1;
			return findSpotNearHorizontalDoorway(door, furniture.getEdge1(), y1, y2);
		}
		
		
		if(currentRoom == this.ROOM2 && targetRoom == this.ROOM1){
			door = this.DOORWAY_ROOMS12;
			y1 = door.getY2() + 1;
			y2 = door.getY2() +furniture.getEdge2() + 1;
			return findSpotNearHorizontalDoorway(door, furniture.getEdge1(), y1, y2);
		}
		
		
		
		if(currentRoom == this.ROOM2 && targetRoom == this.ROOM3){
			door = this.DOORWAY_ROOMS23;
			x1 = door.getX1() - furniture.getEdge1() - 1;
			x2 = door.getX1() - 1;
			return findSpotNearVerticalDoorway(door, furniture.getEdge2(), x1, x2);
		}
		
		if(currentRoom == this.ROOM3 && targetRoom == this.ROOM1){
			door = this.DOORWAY_ROOMS13;
			x1 = door.getX2() + 1; 
			x2 = door.getX2() + furniture.getEdge1() + 1;
			return findSpotNearVerticalDoorway(door, furniture.getEdge2(), x1, x2);
		}
		
		if(currentRoom == this.ROOM3 && targetRoom == this.ROOM2){
			door = this.DOORWAY_ROOMS23;
			x1 = door.getX2() + 1; 
			x2 = door.getX2() + furniture.getEdge1() + 1;
			return findSpotNearVerticalDoorway(door, furniture.getEdge2(), x1, x2);
		}
		return null;
		
	}
	
	/************************************************************************ 
	 *   In our implementation of STRIPS, We use a set of Conditions and 	*
	 *   and Actions. Each set will be implemented as a group of methods.	*
	 ************************************************************************/

	/* ------ Conditions -------*/

	/**
	 * The function check if given rectangle is inside a space
	 * @param rec - the given rectangle
	 * @param space - the space we want to check 
	 * @return : true if the rectangle is fully covered by the space 
	 */
	public boolean InSpace(RecInfo rec, RecInfo space){
		return rec.getX1() >= space.getX1() &&
			rec.getX2() <= space.getX2() &&
			rec.getY1() >= space.getY1() &&
			rec.getY2() <= space.getY2();
	}

	/**
	 * @param
	 * @return
	 */
	public boolean InPlace(RecInfo rect,RecInfo place){
		return rect.getX1() == place.getX1() && rect.getX2() == place.getX2() 
				&& rect.getY1() == place.getY1() && rect.getY2() == place.getY2();
	}

	/**
	 * @param
	 * @return
	 */
	public boolean CanMoveUp(RecInfo rect){
		int x1 = rect.getX1();
		int x2 = rect.getX2();
		int y1 = rect.getY1();
		
		if (y1 == this.ROOM1.getY1()) { //cannot move into wall; maybe it has to be 0
			return false;
		}
		
		if (y1 == this.ROOM2.getY1()){
			if(x1 >= this.DOORWAY_ROOMS12.getX1() 
			&& x2 <= this.DOORWAY_ROOMS12.getX2()){ //Door
				if (game.getRectangleByXAxis(x1, x2, y1-1) == null){
					return true;
				}
				
				return false;
			}
			else if (x1 <2 || (x2 > 5 && x2 <= 7)){//cannot move into wall
				return false;
			}
		}
		
		if (game.getRectangleByXAxis(x1, x2, y1-1) == null){
			return true;
		}
		
		return false;
		
	} // rect can move one step up
	
	/**
	 * @param
	 * @return
	 */
	public boolean CanMoveDown(RecInfo rect){
		int x1 = rect.getX1();
		int x2 = rect.getX2();
		int y2 = rect.getY2();
		
		if (y2 == this.ROOM2.getY2()) { //cannot move into wall; 
			return false;
		}
		
		if (y2 == this.ROOM1.getY2()){
			if(x1 >= this.DOORWAY_ROOMS12.getX1() && x2 <= this.DOORWAY_ROOMS12.getX2()){ //Door
				if (game.getRectangleByXAxis(x1, x2, y2+1) == null){
					return true;
				}
				
				return false;
			}
			else if (x1 <this.DOORWAY_ROOMS12.getX1() 
					|| (x2 > this.DOORWAY_ROOMS12.getX2() 
					&& x2 <= this.ROOM2.getX2())){//cannot move into wall
				return false;
			}
		}
		
		if (game.getRectangleByXAxis(x1, x2, y2+1) == null){
			return true;
		}
		
		return false;
	} // rect can move one step down

	/**
	 * @param
	 * @return
	 */	
	public boolean CanMoveLeft(RecInfo rect){
		int x1 = rect.getX1();
		int y1 = rect.getY1();
		int y2 = rect.getY2();
		
		if (x1 == this.ROOM1.getX1()) { //cannot move into wall; 
			return false;
		}
		
		if (x1 == this.DOORWAY_ROOMS13.getX2()+1){
			if(y1 >= this.DOORWAY_ROOMS13.getY1() && y2 <= this.DOORWAY_ROOMS13.getY2()){ //Door
				if (game.getRectangleByYAxis(x1-1, this.DOORWAY_ROOMS13.getY1(), this.DOORWAY_ROOMS13.getY2()) == null){
					return true;
				}
				return false;
			}
			if(y1 >= this.DOORWAY_ROOMS23.getY1() && y2 <= this.DOORWAY_ROOMS23.getY2()){ //Door
				if (game.getRectangleByYAxis(x1-1, this.DOORWAY_ROOMS23.getY1(), this.DOORWAY_ROOMS23.getY2()) == null){
					return true;
				}
				return false;
			}
			return false;
		}
		
		if (x1 == this.DOORWAY_ROOMS12.getX1() && y1 <= this.DOORWAY_ROOMS12.getY1() && y2 >= this.DOORWAY_ROOMS12.getY2()){
			return false;
		}
		
		if (game.getRectangleByYAxis(x1-1, y1, y2) == null){
			return true;
		}
		
		return false;
	} // rect can move one step left
	
	/**
	 * @param
	 * @return
	 */	
	public boolean CanMoveRight(RecInfo rect){
		int x2 = rect.getX2();
		int y1 = rect.getY1();
		int y2 = rect.getY2();
		
		if (x2 == this.ROOM3.getX2()) { //cannot move into wall; maybe it has to be 0
			return false;
		}
		
		if (x2 == this.DOORWAY_ROOMS13.getX1()-1){
			if(y1 >= this.DOORWAY_ROOMS13.getY1() && y2 <= this.DOORWAY_ROOMS13.getY2()){ //Door
				if (game.getRectangleByYAxis(x2+1, this.DOORWAY_ROOMS13.getY1(), this.DOORWAY_ROOMS13.getY2()) == null){
					return true;
				}
				return false;
			}
			if(y1 >= this.DOORWAY_ROOMS23.getY1() && y2 <= this.DOORWAY_ROOMS23.getY2()){ //Door
				if (game.getRectangleByYAxis(x2+1, this.DOORWAY_ROOMS23.getY1(), this.DOORWAY_ROOMS23.getY2()) == null){
					return true;
				}
				return false;
			}
			return false;
		}
		
		if (x2 == this.DOORWAY_ROOMS12.getX2() && y1 <= this.DOORWAY_ROOMS12.getY1() && y2 >= this.DOORWAY_ROOMS12.getY2()){
			return false;
		}
		
		if (game.getRectangleByYAxis(x2+1, y1, y2) == null){
			return true;
		}
		
		return false;
	} // rect can move one step right
	
	private boolean notEncountersWalls(RecInfo rect){
		int x1 = rect.getX1();
		int x2 = rect.getX2();
		int y1 = rect.getY1();
		int y2 = rect.getY2();
		if (x1 < ROOM1.getX1()){ // under upper wall
			return false;
		}
		if (y1 < ROOM1.getY1()){ //under bottom wall
			return false;
		}
		if (x2 > ROOM3.getX2()){ // under upper wall
			return false;
		}
		
		if (y2 > ROOM2.getY2()){ //under bottom wall
			return false;
		}
		if (y1 < DOORWAY_ROOMS13.getY1() && x1 <= DOORWAY_ROOMS13.getX1() && x2 >= DOORWAY_ROOMS13.getX2()){ //between door and upper wall
			return false;
		}
		if (y1 > DOORWAY_ROOMS13.getY2() && y1 < DOORWAY_ROOMS23.getY1() && x1 <= DOORWAY_ROOMS13.getX1() && x2 >= DOORWAY_ROOMS13.getX2()){ //between doors
			return false;
		}
		if (y2 > DOORWAY_ROOMS13.getY1() && y2 < DOORWAY_ROOMS23.getY1() && x1 <= DOORWAY_ROOMS13.getX1() && x2 >= DOORWAY_ROOMS13.getX2()){ //between doors
			return false;
		}
		if (y2 > DOORWAY_ROOMS23.getY2() && x1 <= DOORWAY_ROOMS23.getX1() && x2 >= DOORWAY_ROOMS23.getX2()){ //between door and bottom wall
			return false;
		}
		if (x1 < DOORWAY_ROOMS12.getX1() && y1 <= DOORWAY_ROOMS12.getY1() && y2 >= DOORWAY_ROOMS12.getY2()){
			return false;
		}
		if (x1 > DOORWAY_ROOMS12.getX2() && x1 < DOORWAY_ROOMS23.getX1() && y1 <= DOORWAY_ROOMS12.getY1() && y2 >= DOORWAY_ROOMS12.getY2()){
			return false;
		}
		if (x2 > DOORWAY_ROOMS12.getX2() && x2 <= DOORWAY_ROOMS23.getX1() && y1 <= DOORWAY_ROOMS12.getY1() && y2 >= DOORWAY_ROOMS12.getY2()){
			return false;
		}
		return true;
	}
	
	
	/**
	 * @param
	 * @return
	 */
	public boolean CanRotateRight(RecInfo rect){
		int x1 = rect.getX1();
		int y1 = rect.getY1();
		int y2 = rect.getY2();
		int edge1 = rect.getEdge1();
		int edge2 = rect.getEdge2();
		
		
		int newX1 = x1;
		int newX2 = x1 + edge2;
		int newY1 = y2+1;
		int newY2 = y2 + edge1+1;
		

		if (notEncountersWalls(new RecInfo(newX1, newX2, newY1, newY2)) == false){
			return false;
		}
		
		return game.IsFree(new RecInfo(newX1, newX2, newY1, newY2));
	} // there is enough space to make right rotation
	
	/**
	 * @param
	 * @return
	 */
	public boolean CanRotateLeft(RecInfo rect){
		int x1 = rect.getX1();
		int y1 = rect.getY1();
		int y2 = rect.getY2();
		int edge1 = rect.getEdge1();
		int edge2 = rect.getEdge2();
		
		int newX1 = x1 - edge2;
		int newX2 = x1-1;
		int newY1 = y2-edge1;
		int newY2 = y2;
		
		
		
		if (notEncountersWalls(new RecInfo(newX1, newX2, newY1, newY2)) == false){
			return false;
		}
		
		return game.IsFree(new RecInfo(newX1, newX2, newY1, newY2));
	} // there is enough space to make left rotation
	
	/**
	 * @param
	 * @return
	 */
	public boolean Rotated(RecInfo rect1, RecInfo rect2){
		return !(rect1.getEdge1() == rect2.getEdge1() && rect1.getEdge2() == rect2.getEdge2());
	} // rect1 is rotated related to rect2
	
	/**
	 * @param
	 * @return
	 */
	public boolean IsLower(RecInfo rect1, RecInfo rect2){
		return rect1.getY1() > rect2.getY1();
	} // rect1 is lower then rect2
	
	/**
	 * @param
	 * @return
	 */
	public boolean IsHigher(RecInfo rect1, RecInfo rect2){
		return rect1.getY1() < rect2.getY1();
	} // rect1 is higher then rect2
	
	/**
	 * @param
	 * @return
	 */
	public boolean IsToTheLeft(RecInfo rect1, RecInfo rect2){
		return rect1.getX1() < rect2.getX1();
	} // rect1 is to the left of rect2
	
	/**
	 * @param
	 * @return
	 */
	public boolean IsToTheRight(RecInfo rect1, RecInfo rect2){
		return rect1.getX1() > rect2.getX1();
	} // rect1 is to the right of rect2


	/* ------ Actions -------*/
	
	/**
	 * @param
	 */
	public void RotateRight(RecInfo rect){
		RecInfo destinationInfo = new RecInfo(rect.getX1(), rect.getX1()+rect.getEdge2(), 
										rect.getY2()+1, rect.getY2()+rect.getEdge1()+1);
		game.Move(rect,  destinationInfo);
		rect.update(destinationInfo);		
	}
	
	/**
	 * @param
	 */
	public void RotateLeft(RecInfo rect){
		RecInfo destinationInfo = new RecInfo(rect.getX1()-1-rect.getEdge2(), rect.getX1()-1, 
				rect.getY2()-1, rect.getY2()+rect.getEdge1()-1);
		game.Move(rect,  destinationInfo);
		rect.update(destinationInfo);		
	}

	/**
	 * @param
	 */	
	public void MoveRight(RecInfo rect){
		RecInfo destinationInfo = new RecInfo(rect.getX1()+1, rect.getX2()+1, 
							rect.getY1(), rect.getY2());
		game.Move(rect,  destinationInfo);
		rect.update(destinationInfo);		
	}
	
	/**
	 * @param
	 */	
	public void MoveLeft(RecInfo rect){
		RecInfo destinationInfo = new RecInfo(rect.getX1()-1, rect.getX2()-1, 
				rect.getY1(), rect.getY2());	
		game.Move(rect,  destinationInfo);
		rect.update(destinationInfo);	
	}

	/**
	 * @param
	 */	
	public void MoveUp(RecInfo rect){
		RecInfo destinationInfo = new RecInfo(rect.getX1(), rect.getX2(), 
				rect.getY1()-1, rect.getY2()-1);		
		game.Move(rect,  destinationInfo);
		rect.update(destinationInfo);
	}

	/**
	 * @param
	 */	
	public void MoveDown(RecInfo rect){
		RecInfo destinationInfo = new RecInfo(rect.getX1(), rect.getX2(), 
				rect.getY1()+1, rect.getY2()+1);
		game.Move(rect,  destinationInfo);
		rect.update(destinationInfo);
	}

/* ----------------------------- Object Methods ---------------------------- */

/* ---------------------------- Private Methods ---------------------------- */

	private static void debugPrint(int debugLevel, String debugText){
		if(debugLevel == CURRENT_DEBUG_LEVEL || CURRENT_DEBUG_LEVEL == DEBUG_ALL){
			System.out.println("Debug print: "+DEBUG_TAG);
			System.out.println(debugText);
		}
	}

 } // End of Class stripsAPI ----------------------------------------------- //