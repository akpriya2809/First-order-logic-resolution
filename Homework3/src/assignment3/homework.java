package assignment3;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class homework {
	int numQueries;
	static ArrayList<String> queries = new ArrayList<>();
	int numKB;
	static ArrayList<String> knowledgeBase = new ArrayList<>();
	HashMap<String, HashSet<String>> wholeKB = new HashMap<>(); //pred, list of questions
	static HashMap<Pattern, Integer> tokenMap = new HashMap<>();
	static ArrayList<String> tokens = new ArrayList<>();
	static LinkedList<String> s = new LinkedList<>();
	static int negCount = 0, andCount = 0, orCount = 0, implCount = 0;
	static LinkedList<String> predicate = new LinkedList<>();
	static LinkedList<LinkedList<String>> sentences = new LinkedList<>();
	static ArrayList<String> answersList = new ArrayList<>();
	static LinkedList<String> KBList = new LinkedList<>();
	static boolean[] varArray = new boolean[26];
	static char[] varList = {'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z'};
	static ArrayList<Boolean> finalOutput = new ArrayList<>();
	static long startTime, endTime;
	static HashMap<String, Integer> variableListMap = new HashMap<>();
	
	public static void main(String[] args) {
		String ptn = "\\(";
		tokenMap.put(Pattern.compile("^("+ptn+")"), 1);
		ptn ="\\)";
		tokenMap.put(Pattern.compile("^("+ptn+")"), 2);
		ptn = "\\,";
		tokenMap.put(Pattern.compile("^("+ptn+")"), 3);
		ptn ="\\#";
		tokenMap.put(Pattern.compile("^("+ptn+")"), 4);
		ptn ="\\&";
		tokenMap.put(Pattern.compile("^("+ptn+")"), 5);
		ptn = "\\|";
		tokenMap.put(Pattern.compile("^("+ptn+")"), 6);
		ptn = "\\~";
		tokenMap.put(Pattern.compile("^("+ptn+")"), 2);
		ptn = "[a-zA-Z][a-zA-Z0-9_]*";
		tokenMap.put(Pattern.compile("^("+ptn+")"), 2);
		

		
		
		File file = new File("/Users/akankshapriya/AI_Assignments/Homework3/src/input.txt");
		Scanner sc;
		homework hw= new homework();
		startTime = System.nanoTime();
		try {
			sc = new Scanner(file);
			if(sc.hasNextLine()) {
				hw.numQueries = Integer.parseInt(sc.nextLine());
			}
			int n = hw.numQueries;
			
			while(sc.hasNextLine() && n>0) {
				queries.add(sc.nextLine().trim());
				n--;
			}
			if(sc.hasNextLine()) {
				hw.numKB = Integer.parseInt(sc.nextLine());
			}
			n = hw.numKB;
			while(sc.hasNextLine() && n>0) {
				knowledgeBase.add(sc.nextLine().trim());
				n--;
			}
			String questions[][] = new String[hw.numQueries][4];
			
			answersList.clear();
			for(int i = 0; i<hw.numQueries; i++) {
				hw.wholeKB.clear();
				sentences.clear();
				KBList.clear();
				Arrays.fill(varArray, false);
				
				
				questions[i][0] = queries.get(i); // complete query
				questions[i][1] = Negate(queries.get(i));// negated complete query
				// store predicate with and without negation
				questions[i][2] = processQuery(questions[i][0]); // predicate
				questions[i][3] = processQuery(questions[i][1]);// negated predicate
				
//				
//				if(hw.wholeKB.containsKey(questions[i][3]))
//                {
//                   HashSet<String> set = hw.wholeKB.get(questions[i][2]);
//                   set.add(questions[i][1]);
//                   hw.wholeKB.put(questions[i][3], set);
//                   KBList.add(questions[i][1]);
//
//                }
//                else
//                {
                	HashSet<String> tempqts= new HashSet<>();
                	tempqts.add(questions[i][1].replaceAll("\\s+", ""));
                	hw.wholeKB.put(questions[i][3],tempqts);
                	KBList.add(questions[i][1].replaceAll("\\s+", ""));
                //}
				for(int j =0; j<hw.numKB; j++) {
					generateTokens(knowledgeBase.get(j).replaceAll("\\s+", "").replaceAll("=>", "#"));
					LinkedList<String> preds = new LinkedList<>(predicate);
					sentences.add(preds);
				}
				addPredstoKB(hw.wholeKB, true);
				
				boolean  ans = getAnswer(hw.wholeKB);
				finalOutput.add(ans);
				
			}
			//System.out.println(finalOutput);
			//System.out.println(KBList);
			writetoOutputFile();
//			endTime = System.nanoTime();
//			double duration = (endTime- startTime)/1_000_000_000;
//			System.out.println(duration+" secs");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

	}
	private static void writetoOutputFile() {
		FileWriter writer;
		try {
			writer = new FileWriter("/Users/akankshapriya/AI_Assignments/Homework3/src/output.txt");
			
			int size = finalOutput.size();
			for(int j = 0; j<size; j++) {
					if(finalOutput.get(j)) {
						writer.write("TRUE");
					}else {
						writer.write("FALSE");
					}
					if(j!=size-1) {
						writer.write("\r\n");
					}
				
				}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		
	}
	private static boolean getAnswer(HashMap<String, HashSet<String>> wholeKB) {
		int num =0;
		
		outer:
		while(true) {
			//System.out.println("-------------------------------------------------------");
			List<String> newSentences = new ArrayList<>();
			for(int i = 0; i<KBList.size(); i++) {
				for(int j =i+1; j<KBList.size(); j++) {
					long currTime = System.nanoTime();
					double timeSpent = (currTime-startTime)/1_000_000_000;
					System.out.println("timeSpent"+timeSpent);
					if(timeSpent>=120) {
						break outer;
					}
					String[] arr1 = KBList.get(i).split("\\|");
					ArrayList<String> firstList = new ArrayList<>(Arrays.asList(arr1));
					String[] arr2 = KBList.get(j).split("\\|");
					ArrayList<String> secondList = new ArrayList<>(Arrays.asList(arr2));
					List<String> newSentencesList = new ArrayList<>();
					
//					System.out.println("i:"+i+"j::"+j);
//					System.out.println("num::"+num+"firstList"+firstList);
//					System.out.println("secondList"+secondList);
					boolean isUnify = resolve(firstList, secondList, newSentencesList);
					if(isUnify) {
						newSentences.addAll(newSentencesList);
						if(newSentencesList.size()==0) {
							return true;
						}
						
						
					}
					
				}
			}
			
			sentences.clear();
			boolean isAllPresent = addNewSentencestoKBList(newSentences);
			if(isAllPresent) {
				 return false;
			}
			
			for(int t = 0; t<newSentences.size(); t++) {
				generateTokens(newSentences.get(t));
				LinkedList<String> preds = new LinkedList<>(predicate);
				sentences.add(preds);
			}
			addPredstoKB(wholeKB, false);
//			System.out.println("Round:"+num);
//			for(String kb: KBList) {
//				System.out.println(kb);
//			}
			num++;
		}
		return false;
		
	}


	private static boolean resolve(ArrayList<String> alphaList, ArrayList<String> arrList,
			List<String> newSentencesList) {
		
		boolean atLeastOneUnify = false;
		int i = 0;
		while(i < alphaList.size()) {
			String premise = alphaList.get(i);
			String pred = processQuery(alphaList.get(i));
			String negatedPred = Negate(pred);
			LinkedList<String> args1=findArgs(premise);
			int j =0;
			boolean isRemoved = false;
			while( j<arrList.size()) {
				String arrPred = processQuery(arrList.get(j));
				if(arrPred.equals(negatedPred)) {
					LinkedList<String> args2=findArgs(arrList.get(j));
					if(args1.size()==args2.size()) {
						if(alphaList.size()!=0  && arrList.size()!=0) {
							HashMap<String, String> substitutionList = new HashMap<>();
							boolean isUnify = doUnification(alphaList.get(i), arrList.get(j), args1, args2, substitutionList);
							if(isUnify) {
								atLeastOneUnify = true;
								ArrayList<String> tmpString= new ArrayList<>();
								for(int p = 0 ; p< alphaList.size();p++) {
									if(p !=i) {
										tmpString.add(alphaList.get(p));
									}
								}
								for(int q = 0;q< arrList.size();q++) {
									if(q!=j) {
										tmpString.add(arrList.get(q));
									}
								}
								if(!substitutionList.isEmpty()) {
									replaceVariables(tmpString, substitutionList);
								}
								ArrayList<String> temp = new ArrayList<>();
								for(String tmp:tmpString) {
									if(!temp.contains(tmp)) {
										temp.add(tmp);
									}
								}
								Collections.sort(temp);
								if(!temp.isEmpty()) {
									String finalStr = String.join("|", temp);
									newSentencesList.add(finalStr);
								}
								
								
							}
						}
					}
				}
				j++;
			}
			i++;
		}
		return atLeastOneUnify;

	}
	private static boolean addNewSentencestoKBList(List<String> newSentences) {
		boolean isAllPresent = false;
		int count = 0;
		for(String s: newSentences) {
				if(KBList.contains(s)) {
					count++;
				}else {
					KBList.add(s);
				}
			
		}
		if(count == newSentences.size()) {
			isAllPresent = true;
		}
		return isAllPresent;
	
	}
	private static void replaceVariables(ArrayList<String> arrList, HashMap<String, String> substitutionList) {
		for(int t = 0; t<arrList.size();t++) {
			String s = arrList.get(t);
			LinkedList<String> args1=findArgs(s);
			LinkedList<String> replaceArgs = new LinkedList<>();
			String pred = processQuery(s);
			for(String arg: args1) {
				if(!Character.isUpperCase(arg.charAt(0))) {
					//String strArg = String.valueOf(arg.charAt(0));
					if(substitutionList.containsKey(arg)) {
						replaceArgs.add(substitutionList.get(arg));
					}else {
						replaceArgs.add(arg);
					}
				}else {
					replaceArgs.add(arg);
				}
				
			}
			String replacedString = pred;
			if(replaceArgs.size()>0) {
				replacedString+="(";
				String joinedArgs = String.join(",", replaceArgs);
				replacedString+=joinedArgs;
				replacedString+=")";
			}
			arrList.remove(t);
			arrList.add(t, replacedString);
		}
		
	}


	private static boolean  doUnification( String premise, String kbstr, LinkedList<String> args1, 
			LinkedList<String> args2, HashMap<String, String> substitutionList) {
		//System.out.println("doUnification");
		int len = args1.size();
		//boolean isUnify = true;
		for(int i =0; i<len ; i++) {
			if(isConstant(args1.get(i)) && isConstant(args2.get(i))){
				if(!args1.get(i).equals(args2.get(i))) {
					return false;
				}
			}else if(!isConstant(args1.get(i)) && isConstant(args2.get(i))) {
				substitutionList.put(args1.get(i), args2.get(i));
				//unify conclusion with args1
			}else if(isConstant(args1.get(i)) && !isConstant(args2.get(i))) {
				substitutionList.put(args2.get(i), args1.get(i));
			}else if(!isConstant(args1.get(i))&& !isConstant(args2.get(i))){
				if(!args1.get(i).equals(args2.get(i))) {
					return false;
				}
				
			}
		}
		return true;
	}

	private static boolean isConstant(String string) {
		if(Character.isUpperCase(string.charAt(0))) {
			return true;
		}
		return false;
	}

	private static LinkedList<String> findArgs(String str) {
		LinkedList<String> argList = new LinkedList<>();
		int i = 0;
		while(str.charAt(i) !='(') {
			i++;
		}
		String temp="";
		for(int j = i+1; j<str.length(); j++) {
			char ch = str.charAt(j);
			if(ch == ',') {
				argList.add(temp);
				temp ="";
			}else if (ch == ')'){
				argList.add(temp);
				break;
			}else {
				temp+=ch;
			}
			
		}
		return argList;
	}

	private static void generateTokens(String str) {
		String s = str.trim();
		tokens.clear();
		predicate.clear();
		while(!s.equals("")) {
			
			for(Pattern p :tokenMap.keySet()) {
				Matcher m = p.matcher(s);
				if(m.find()) {
					//check for case when not matched
		        	String tok = m.group().trim();
			        s = m.replaceFirst("").trim();
			        tokens.add(tok);
			        break;
				}
			}
		}
		
		parser();

	}

	private static void parser() {
		
		int i = 0, j=0;
		String temp="";
		int l = tokens.size();
		Map<Character, Character> varSubsMap = new HashMap<>();
		HashSet<String> set = new HashSet<>();
		while(i <l) {
			String tok = tokens.get(i);
			if(tok.equals("&")) {
				andCount++;
				temp+=tok;
			}
			if(tok.equals("~")) {
				negCount++;
				temp+=tok;
			}
			if(tok.equals("|")) {
				orCount++;
				temp+=tok;
			}
			if(tok.equals("#")) {
				implCount++;
				temp+=tok;
			}
			
			if(Character.isUpperCase(tok.charAt(0))) {
				temp+=tok;
				 j = i+1;
				while(j<l) {
					tok = tokens.get(j);
					if(!isConstant(tok) && Character.isLowerCase(tok.charAt(0))) {
						if(!variableListMap.containsKey(tok.charAt(0)+"")) {
							set.add(tok);
							//variableListMap.put(tok.charAt(0)+"", 1);
						}else {
							Integer cnt = variableListMap.get(tok.charAt(0)+"");
							cnt++;
							//variableListMap.put(tok.charAt(0)+"",cnt);
							tok=tok+cnt+"";
							set.add(tok);
							//variableListMap.put(tok.charAt(0)+cnt+"",1);
						}
						
//						if(varArray[tok.charAt(0)-'a']) {
//							Character subs = varSubsMap.get(tok.charAt(0));
//								if(subs!=null) {
//									tok = subs+"";
//									set.add(tok);
//								}else {
//									for(int k =0; k<26;k++) {
//										if(!varArray[k]) {
//											varSubsMap.put(tok.charAt(0),varList[k]);
//											
//											tok=varList[k]+"";
//											varArray[tok.charAt(0)-'a']=true;
//											set.add(tok);
//											break;
//										}
//									}
//								}
//							
//							
//						}else {
//							set.add(tok);
//						}
					}
					if(tok.equals(")")){
						temp+=tok;
						break;
					}else {
						temp+=tok;
					}
					j++;
				}
				predicate.add(temp);
				temp="";
				i=j+1;
			}else {
				i++;
				predicate.add(temp);
				temp="";
			}
				

		}
		for(String var: set) {
			if(variableListMap.containsKey(var.charAt(0)+"")) {
				Integer count = variableListMap.get(var.charAt(0)+"");
				variableListMap.put(var.charAt(0)+"", count+1);
				variableListMap.put(var, 1);
			}else {
				variableListMap.put(var, 1);
			}
			
		}
//		for(String var: set) {
//			varArray[var.charAt(0)-'a']=true;
//		}
		varSubsMap.clear();
		if(implCount>0) {
			removeImplySign(implCount);
		}
		
	}

	private static void addPredstoKB(HashMap<String, HashSet<String>> wholeKB, boolean fromInitial) {
		String temp ="";
		
				for(LinkedList<String> predlist:sentences) {
					for(String s: predlist) {
						if(s.equals("~")) {
							temp+=s;
							
						}
						//System.out.println(predlist);
						if (Character.isUpperCase(s.charAt(0))) {
							
							int i =0;
							while(s.charAt(i)!='(') {
								temp+=String.valueOf(s.charAt(i));
								i++;
							}
							if(wholeKB.containsKey(temp) ) {
								HashSet<String> list = wholeKB.get(temp);
								StringBuilder sb  = new StringBuilder();
								for(String st:predlist) {
									sb.append(st);
								}
								if(!KBList.contains(sb.toString()) && fromInitial) {
									KBList.add(sb.toString());
								}
								list.add(sb.toString());
								wholeKB.put(temp, list);
								
								temp="";
							}else {
								HashSet<String> newList = new HashSet<>();
								StringBuilder sb  = new StringBuilder();
								for(String st:predlist) {
									sb.append(st);
								}
								if(!KBList.contains(sb.toString()) && fromInitial) {
									KBList.add(sb.toString());
								}
								newList.add(sb.toString());
								wholeKB.put(temp,newList);
								temp="";
								
							}
						}
					}
				}	
		
		
	}

	private static void removeImplySign(int count) {
		 int lhs = -1;
	        int l = 0;
	            lhs = 0;
	            LinkedList<String> temp = new LinkedList<>();
	            int check = 0;
	            for(int i =0 ; i<predicate.size(); i++) {
            	if(predicate.get(i).equals("#")) {
            		predicate.remove(i);
            		implCount --;
            		predicate.add(i, "|");
            		lhs=i;  
            		break;
            	}
            }
	            for(int i =0; i<lhs;i++) {
	            	String str = predicate.get(i);
	            	if(str.equals("~")) {
	            		check++;
	            	}else if(str.equals("&")) {
	            		temp.add("|");
	            	}else if (check>0) {
	            		check--;
	            		temp.add(str);
	            	}else {
	            		temp.add("~");
	            		temp.add(str);
	            	}
	            }
	            while(lhs<predicate.size()){
	            	temp.add(predicate.get(lhs));
	            	lhs++;
	            }
	            predicate = (LinkedList<String>)temp.clone();
	}

	private static String processQuery(String query) {
		char[] arr = query.toCharArray();
		int i=0;
		String returnstr="";
    	while(arr[i]!='(')
    	{
    		returnstr=(returnstr+String.valueOf(arr[i]));
    		i++;
    	}
    	return returnstr;
		
	}
	private static  String Negate(String query) {
		query = query.trim();
		String str="";
		if(query.charAt(0)== '~') {
			 str = query.substring(1);
		}else {
			str ='~'+query;
		}
		return str;
		
	}
}
