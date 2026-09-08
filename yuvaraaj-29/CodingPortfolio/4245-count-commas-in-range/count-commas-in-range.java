class Solution {
    public int countCommas(int n) {
        if(n<1000) return 0;
        else{
            n = n - 1000;
            n+=1;
        }
        return n;
    }
}