class Solution {
    public int[] resultArray(int[] nums) {
        ArrayList<Integer> l1 = new ArrayList<>();
        ArrayList<Integer> l2 = new ArrayList<>();
        l1.add(nums[0]);
        l2.add(nums[1]);
        for(int i=2;i<nums.length;i++){
            if(l1.get(l1.size()-1)>l2.get(l2.size()-1)){
                l1.add(nums[i]);
            }else{
                l2.add(nums[i]);
            }
        }
        int[] arr = new int[nums.length];
        int k=0;
        for(int i=0;i<l1.size();i++){
            arr[i] = l1.get(i);
            k=i;
        }
        k+=1;
        for(int i=0;i<l2.size();i++){
            arr[k++] = l2.get(i);
        }
        return arr;
    }
}