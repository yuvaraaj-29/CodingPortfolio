class Solution {
    public int smallestIndex(int[] nums) {
        for(int i=0;i<nums.length;i++){
            int sum=0;
            int n = nums[i];
            while(n>0){
                int r = n % 10;
                sum+=r;
                n/=10;
            }
            if(sum==i){
                return i;
            }
        }
        return -1;
    }
}