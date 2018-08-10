package utils;

public class CompactQueue {

	private int[][] items;
	private int index = 0;

	public CompactQueue(int itemCount, int packetCount){

		if(packetCount > 0 & itemCount > 0){
			/* The 'remainder' packets will have one fewer item 
			 * than the 'main' packets. */
			int nMain = itemCount % packetCount;

			/* Integer division truncates down. */
			int nRemainderItems = itemCount / packetCount;

			items = new int[][] {
				{nMain, packetCount - nMain}, 
				{nRemainderItems + 1, nRemainderItems}
			};
		}
		else{
			items = new int[][] {{0, 0}, {0, 0}};
		}
	}

	public int next(){
		if(index < items[0][0]){
			index++;
			return items[1][0];
		}
		else if(index < items[0][0] + items[0][1]){
			index++;
			return items[1][1];
		}
		else return 0;
	}
}
