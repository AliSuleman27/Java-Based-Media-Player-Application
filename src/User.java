
import java.util.ArrayList;



class User {
   static int count=0;
   int id;
   String fname;
   String lname;
   String username;
   String password;
   String personal_directory;
   String history_directory;
   
   User(String[] fields)
   {
       count++;
       this.id = Integer.parseInt(fields[0]);
       this.fname = fields[1];
       this.lname = fields[2];
       this.username = fields[3];
       this.password = fields[4];
       this.personal_directory = fields[5];
       this.history_directory = fields[6];

   }
   
   
   User(String fname, String lname, String uname, String pass, String pdir, String hdir)
   {
       count++;
       this.id = count;
       this.username = uname;
       this.password = pass;
       this.personal_directory = pdir;
       this.lname = lname;
       this.fname = fname;
       this.history_directory = hdir;
   }
   
   
   User()
   {
       
   }
   
   User(User u)
   {
       id = u.id;
       username = u.username;
       password = u.password;
       personal_directory = u.personal_directory;
       fname = u.fname;
       lname = u.lname;
       history_directory = u.history_directory;
   }
   
   String user_to_string()
   {
       return (id +  ";" +fname + ";" + lname + ";"+username + ";" + password + ";" + personal_directory+";"+history_directory);
   }
   
   boolean validateUsername(String typed)
   {
       if(this.username.length() != typed.length())
       {
           return false;
       }
       else
       {
           if(this.username.equals(typed))
           {
               return true;
           }
           else
           {
               return false;
           }
       }
   }
   
  
   
   //void merge(ArrayList<User>)
   boolean isExistent(ArrayList<User> allUsers, String username)
   {
      int mid;
      int start = 0;
      int end= allUsers.size() - 1;
      while(start<=end){
          mid = (start+end)/2;
          String midemail = allUsers.get(mid).username.toLowerCase();
          int cmp = username.toLowerCase().compareTo(midemail);
          if(cmp==0){
              return true;
          }
          else if(cmp>0){
              start = mid+1;
              
          }
          else{
              end = mid-1;
              
          }
      }
      return false;
   }
}
