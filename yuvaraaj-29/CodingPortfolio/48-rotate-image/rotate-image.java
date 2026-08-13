class Solution {
    public void rotate(int[][] arr) {
        int[][] m = new int[arr.length][arr[0].length];
        for(int i=0;i<arr[0].length;i++){
            for(int j=0;j<arr.length;j++){
                m[i][j] = arr[j][i];
            }
        }
        int k=0;
        for(int i=0;i<arr.length;i++){
            for(int j=arr.length-1;j>=0;j--){
                arr[i][k++] = m[i][j];
            }
            k=0;
        }
    }
}