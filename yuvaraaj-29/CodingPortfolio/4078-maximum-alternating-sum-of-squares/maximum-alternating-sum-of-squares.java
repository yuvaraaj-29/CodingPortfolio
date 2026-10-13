class Solution {
    public long maxAlternatingSum(int[] nums) {
        long[] arr = new long[nums.length];
        for(int i=0;i<nums.length;i++){
            arr[i] = (long)nums[i] * nums[i]; 
        }
        Arrays.sort(arr);
       // System.out.print(Arrays.toString(arr));
        long sum =0;
        for(int i=0;i<nums.length/2;i++){
            sum -= arr[i];
        }
        for(int i=nums.length/2;i<nums.length;i++){
            sum += arr[i];
        }
        return sum;
    }
}