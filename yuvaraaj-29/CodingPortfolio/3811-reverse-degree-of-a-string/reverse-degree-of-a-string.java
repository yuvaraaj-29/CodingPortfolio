class Solution {
    public int reverseDegree(String s) {
        HashMap<Character,Integer> hm = new HashMap<>();
        char a = 'a';
        for(int i=26;i>=0;i--){
            hm.put(a,i);
            a++;
        }
        int k=1,sum=0;
        for(char ch : s.toCharArray()){
            if(hm.containsKey(ch)){
                sum+=hm.get(ch) * k;
                k++;
            }
        }
        return sum;
    }
}