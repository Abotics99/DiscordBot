import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

public class UserData {
	
	private ArrayList<ArrayList<String>> arr;
	
	public UserData() {
		arr = new ArrayList<ArrayList<String>>();
	}
	
	public void AddUser(String userName) {
		if(arr.size()==0) {
			AddVariable("UserID");
			AddUser(userName);
		}else {
			if(!UserExists(userName)) {
				AddRow();
				SetVariable("UserID", arr.size()-2, userName);
				System.out.println("Adding user \"" + userName + "\" to the list...");
			}else {
				System.out.println("User \"" + userName + "\" already exists.");
			}
		}
	}
	
	public int GetUserIndex(String userName) {
		if(UserExists(userName)) {
			for(int i=1;i<arr.size();i++) {
				if(GetVariable("UserID", i-1).equals(userName)) {
					return  i-1;
				}
			}
			return -1;
		}else {
			System.out.println("User \"" + userName + "\" does not exist.");
			return -1;
		}
	}
	
	public void SetUserVariable(String userName, String varName, String value) {
		if(UserExists(userName)) {
			SetVariable(varName, GetUserIndex(userName), value);
		}else {
			System.out.println("User \"" + userName + "\" does not exist.");
		}
	}
	
	public String GetUserVariable(String userName, String varName) {
		if(UserExists(userName)) {
			return GetVariable(varName, GetUserIndex(userName));
		}else {
			System.out.println("User \"" + userName + "\" does not exist.");
			return "null";
		}
	}
	
	public boolean UserExists(String userName) {
		if(arr.size()==0) {
			return false;
		}else {
			for(int i=1;i<arr.size();i++) {
				if(GetVariable("UserID", i-1).equals(userName)) {
					return true;
				}
			}
			return false;
		}
	}
	
	public void AddVariable(String varName){
		if(arr.size()==0) {
			System.out.println("List is empty, creating first row of list...");
			arr.add(new ArrayList<String>());
		}
		ArrayList<String> rowData = arr.get(0);
		
		if(rowData.contains(varName)) {
			System.out.println("The variable name \"" + varName + "\" already exists...");
		}else {
			System.out.println("Creating variable name: \"" + varName + "\"");
			rowData.add(varName);
		}
		
		if(arr.size()>1) {
			for(int i =1;i<arr.size();i++) {
				arr.get(i).add("null");
			}
		}
	}
	
	public void AddRow() {
		arr.add(new ArrayList<String>());
		if(arr.get(0).size()>0) {
			for(int i =0;i<arr.get(0).size();i++) {
				arr.get(arr.size()-1).add("null");
			}
		}
	}
	
	public void SetVariable(String varName, int  rowIndex, String value) {
		if(VariableExist(varName)) {
			int variableIndex = arr.get(0).indexOf(varName);
			arr.get(rowIndex+1).set(variableIndex, value);
		}else {
			System.out.println("Variable \"" + varName + "\" does not exist, adding it now...");
			AddVariable(varName);
			SetVariable(varName, rowIndex, value);
		}
	}
	
	public boolean VariableExist(String varName) {
		return arr.get(0).contains(varName);
	}
	
	public String GetVariable(String varName, int  rowIndex) {
		if(VariableExist(varName)) {
			int variableIndex = arr.get(0).indexOf(varName);
			return arr.get(rowIndex+1).get(variableIndex);
		}else {
			System.out.println("Variable \"" + varName + "\" does not exist...");
			return null;
		}
	}
	
	public void SaveData(String filename){
		try (PrintWriter out = new PrintWriter(filename + ".txt")) {
		    for(ArrayList<String> tempArr: arr) {
		    	for(String strTmp: tempArr) {
		    		out.print("{" + strTmp + "}");
		    	}
		    	out.println();
		    }
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void LoadData(String filename){
		File file = new File(filename + ".txt");
		arr = new ArrayList<ArrayList<String>>();
		Scanner sc;
		try {
			sc = new Scanner(file);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			System.out.println("File not found, creating one now...");
			SaveData(filename);
			LoadData(filename);
			return;
		}
		
		while(sc.hasNextLine()) {
			String line = sc.nextLine();
			ArrayList<String> row = new ArrayList<String>();
			while(line.length()>0) {
				line = line.substring(line.indexOf('{')+1);
				String varChunk = line.substring(0,line.indexOf('}'));
				line = line.substring(line.indexOf('}')+1);
				row.add(varChunk);
			}
			arr.add(row);
		}
		sc.close();
	}
}
