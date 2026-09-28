class Solution {
    public int maxDepth(String s) {
        int max=0,min=0;
        for(int i=0;i<s.length();i++){
            if(s.charAt(i)=='(') max+=1;
            if(s.charAt(i)==')') max-=1;
            min = Math.max(min,max);
        }
        return min;
    }
}