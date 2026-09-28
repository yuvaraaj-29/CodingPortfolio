class Solution {
    public boolean isGoodArray(int[] nums) {
        int g = nums[0];
        if(nums.length==1 && nums[0]==1) return true;
        for (int i = 1; i < nums.length; i++) {
            g = gcd(g, nums[i]);
            if (g == 1) return true;
        }
        return false;
    }
    int gcd(int a, int b) {
        return (b == 0) ? a : gcd(b, a % b);
    }
}