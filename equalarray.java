import java.io.*;
class equalarray
{
  static void merging(int[] a, int l ,int r ,int mid)
   {
      int l1 = mid - l +1;
      int r1 = r-mid;
      int[] left = new int[l1];
      int[] right = new int[r1];
      int i,j;
      for(i=0;i<l1;i++)
      {
         left[i] = a[l+i];
      }
      for(j=0;j<r1;j++)
      {
         right[j] = a[mid+1+j];
      }
      int k = l;
      i=0;
      j=0;
      while(i<l1 && j<r1)
      {
         if(left[i]<=right[j])
         {
            a[k] = left[i];
            i++;
         }
         else
         {
            a[k] = right[j];
            j++;
         }
         k++;
      }
      while(i<l1)
      {
         a[k] = left[i];
         i++;
         k++;
      }
      while(j<r1)
      {
         a[k] = right[j];
         j++;
         k++;
      }
   }
   static void merge(int a[],int l,int r)
   {
    if(l<r)
    {
      int mid = l+(r-l)/2;
      merge(a,l,mid);
      merge(a,mid+1,r);
      merging(a,l,r,mid);
    }
}
   public static void main(String[] args)
   {
      int[] a = {5,4,3,7,6,8,1,2};
      int[] b = {5,4,3,7,6,8,2,8};
      int n = a.length;
      int n1 = b.length;
      if(n != n1)
      {
        System.out.println("arrays aren't equal");
      }
      boolean found = false;
      merge(a,0,n-1); 
      merge(b,0,n1-1);
      for(int i=0;i<n;i++)
      {
         if(a[i] == b[i])
         {
            found = true;
         }
         else{
            found = false;
            break;
         }
      }
      if(found)
      {
        System.out.println("true");
     }
     else{
        System.out.println("false");
     }
   }
}

