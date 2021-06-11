
public class Buckets {
	
	/**
	 * Traverse matrix in spiral order
	 */
	public static void spiral() {
		
		int m = 1;
		int n = 2;
		
		int numIfs = 0;
		int[][] mat = new int[m][n];

		// find middle
		int len = Math.min(m, n);
		int ip = (m - 1) / 2;
		int jp = (n - 1) / 2;

		// spiral through center square
		System.out.print(mat[ip][jp] + ", ");
		int numJumps = 1;
		while (true) {

			// go right -->
			if (numJumps < len) {
				numIfs++;
				for (int i = 0; i < numJumps; i++) {
					numIfs++;
					jp++;
					System.out.print(mat[ip][jp] + ", ");
				}
			} else {
				for (int i = 0; i < numJumps - 1; i++) {
					numIfs++;
					jp++;
					System.out.print(mat[ip][jp] + ", ");
				}
				break;

			}

			// go down |
			// ~~~~~~~ v
			for (int i = 0; i < numJumps; i++) {
				numIfs++;
				ip++;
				System.out.print(mat[ip][jp] + ", ");
			}
			numJumps++;

			// go left <--
			if (numJumps < len) {
				numIfs++;
				for (int i = 0; i < numJumps; i++) {
					numIfs++;
					jp--;
					System.out.print(mat[ip][jp] + ", ");
				}
			} else {
				for (int i = 0; i < numJumps - 1; i++) {
					numIfs++;
					jp--;
					System.out.print(mat[ip][jp] + ", ");
				}
				break;
			}

			// go up ^
			// ~~~~~ |
			for (int i = 0; i < numJumps; i++) {
				numIfs++;
				ip--;
				System.out.print(mat[ip][jp] + ", ");
			}
			numJumps++;

			System.out.println();
		}

		// travel through remaining spaces
		System.out.println();
		System.out.println("numIfs: " + numIfs);
		System.out.println("sides");
	}
}
