import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
/**
 *
 * The goal of this problem set is to build a personal digital assistant named "Sudi"
* @author Dejanay Pinto
 *
 * */

public class Sudi {
    public static String start = "#";
    public static Map<String, Map<String, Double>> transitions  = new HashMap<>(); //word in sentence ->
    public static Map<String, Map<String, Double>> observations = new HashMap<>(); //word in sentence ->    //why is it a map of maps? you have a map for the words and the other map stores the tags and the counts
    public static double bestScore;
    public static String bestString;



    /**
     *
     * This method takes two file and trains them to the Viterbi code
     * @throws IOException
     * @param sentenceFile
     * @param tagFile
     * @purpose create the observation and transition map
     */

    public static void model_training(String sentenceFile, String tagFile) throws IOException {

         transitions = new HashMap<>();
         observations = new HashMap<>();
        // Observations
        BufferedReader file = new BufferedReader(new FileReader(sentenceFile));
        BufferedReader ex = new BufferedReader(new FileReader(tagFile));
        String line;
        String line2;
        try {
            while ((line = file.readLine()) != null && (line2 = ex.readLine()) != null) {
                // split each by line "." so get a sentence
                String[] words = line.toLowerCase().split(" ");
                String[] tags = line2.toLowerCase().split(" ");

                //adds the starting parts of speech to the transition map under the start key
                // each line is supposed to start with a hashtag so the algorithm knows it's the
                // beginning of the sentence

                if (transitions.containsKey(start)) {
                    if (transitions.get(start).containsKey(tags[0])) {
                        transitions.get(start).put(tags[0], (transitions.get(start).get(tags[0]) + 1));
                    } else {
                        transitions.get(start).put(tags[0], 1.0);
                    }
                } else {
                    Map<String, Double> mapFreq = new HashMap<>();
                    mapFreq.put(tags[0], 1.0);
                    transitions.put(start, mapFreq);
                }



                for (int i = 0; i < words.length && i < tags.length; i++) {
                    String tempWord = words[i];
                    String tempTag = tags[i];

                    if (observations.containsKey(tempTag))    //is the word already there
                    {
                        if (observations.get(tempTag).containsKey(tempWord))  //is the tag already there
                        {
                            observations.get(tempTag).put(tempWord, (observations.get(tempTag).get(tempWord)) + 1);  //if the word is already in the map as that tag, the count is incremented
                        } else //word is there but the tag is not
                        {

                            observations.get(tempTag).put(tempWord, 1.0);
                        }
                    } else  //the word is not there
                    {
                        // if not, create the key and value pair and make the count 1
                        Map<String, Double> mapFreq = new HashMap<>();
                        mapFreq.put(tempWord, 1.0);
                        observations.put(tempTag, mapFreq);
                    }

                    if (i != 0)       //there will only be a previous state only after the first word
                    {
                        String prevState = tags[i - 1]; //ensures that prevState is always before the current tag

                        if (transitions.containsKey(prevState)) {
                            if (transitions.get(prevState).containsKey(tempTag)) { // if the transition between the two tags exists, the count is incremented

                                transitions.get(prevState).put(tempTag, (transitions.get(prevState).get(tempTag) + 1));
                            } else {
                                transitions.get(prevState).put(tempTag, 1.0);

                            }

                        } else // if not, create the key and value pair and make the count 1
                        {
                            Map<String, Double> mapFreq = new HashMap<>();
                            mapFreq.put(tempTag, 1.0);
                            transitions.put(prevState, mapFreq);

                        }
                    }

                }
            }
        }
        catch (Exception e)
        {
            System.out.println("The file is empty");
        }
        file.close();
        ex.close();
        probability(transitions);
        probability(observations);

    }


        /**
         *  This method finds the probability of each frequency and takes the log of that, then updates the map accordingly
         *
         *
         *
         * @param temp
         *
         */
    public static void probability(Map<String, Map<String, Double>> temp)
    {

        for(String string : temp.keySet())
        {
           int total = 0;
           for (String str : temp.get(string).keySet())
           {
               total += temp.get(string).get(str);
           }
           for (String str : temp.get(string).keySet())
           {
               temp.get(string).put(str,  Math.log(temp.get(string).get(str)/total));
           }
        }


    }

        /**
         * This method finds the most likely sequence of tags for a given sentence
         * @param str
         * @return List<String>
         */
        public static ArrayList<String> ViterbiDecoding(String str) {
            int unseen = -100;  //value to be added if the word is not present as that part of speech
            double nextScore;
            Map<String, String> currStates = new HashMap<String, String>();  //stores the current states to be examined
            currStates.put(start,null); //the star '#' is added to the current states
            Map<String, Double> currScores = new HashMap<>();  //this stores the scores calculated per part of speech
            ArrayList<Map<String, String>> backTrack = new ArrayList<>();
            currScores.put(start, 0.0);  //start is added to the current scores map and given a value of 0
            String[] c = str.toLowerCase().split(" ");
            ArrayList<String> parts_of_speech = new ArrayList<>();



            //goes through each word in the string entered as the parameter
            for (int i = 0; i <= c.length - 1; i++)
               {
                Map<String, String> nextStates = new HashMap<String, String>();
                Map<String, Double> nextScores = new HashMap<>();
                Map<String, String> tempMap = new HashMap<>();
                bestScore = Double.NEGATIVE_INFINITY;    //initializes the bestScore to the negative infinity so that naturally, every score would be greater than it, and it can be easily updated


                // transition from each current state to each of its next states
                for (String currState : currStates.keySet())
                {
                    if (transitions.containsKey(currState))  //boundary case to ensure that current state is in the transition map
                    {
                        for (String nextState : transitions.get(currState).keySet())
                        {
                            nextStates.put(nextState,currState);
                            if (observations.get(nextState).containsKey(c[i])) //checks if the word is in the observations map as that part of speech
                            {
                               nextScore = currScores.get(currState) + transitions.get(currState).get(nextState) + observations.get(nextState).get(c[i]);     //a potential issue might be that currScores might not have currState
                            }
                            else  //if the word is not that there, an unseen value is used to calculate the nextScore value
                            {
                                nextScore = currScores.get(currState) + transitions.get(currState).get(nextState) + unseen;
                            }

                            //addition to the nextScores map
                            if (!nextScores.containsKey(nextState) || nextScore > nextScores.get(nextState)) {
                                nextScores.put(nextState, nextScore);
                                tempMap.put(nextState, currState);
                                if (nextScore > bestScore)
                                {
                                    bestString = nextState;
                                    bestScore = nextScore;
                                }
                            }
                        }
                    }
                }

                currStates = nextStates;
                currScores = nextScores;
                backTrack.add(tempMap);
                }

            //adds the part of speech with the highest score to the parts of speech list
            parts_of_speech.add(bestString);

            //backTrack is used to then find the likely path given the likely part of speech of the last word
            for (int i = backTrack.size()-1; i>0; i--)
            {
             bestString = backTrack.get(i).get(bestString);
             parts_of_speech.add(0,bestString);
            }
             return parts_of_speech;
            }



        /**
         *
         * This method evaluates the performance of the viterbi code by comparing the likely part of speech of the line
         * to the actual tags fle
         *
         * This is the file testing method
         *
         * @param sentence_file
         * @param tag_file
         */

        public static void performance_eval(String sentence_file, String tag_file)throws IOException
        {

             BufferedReader file = new BufferedReader(new FileReader(sentence_file));
             BufferedReader file1 = new BufferedReader(new FileReader(tag_file));


             String line;
             String line1;
             int wrong = 0, right = 0;

             try {
                 while ((line = file.readLine()) != null && (line1 = file1.readLine()) != null) {
                     ArrayList<String> tags = ViterbiDecoding(line);

                     String[] str = line1.toLowerCase().split(" ");
                     for(int x=0;x<=tags.size()-1;x++)
                     {
                         if (tags.get(x).equals(str[x]))
                         {
                                   right++;
                         }
                         else
                         {
                                   wrong++;
                         }

                     }

                 }
             }
             catch (Exception e)
             {
                 System.out.println("I/O exception");
             }

             file.close();
             file1.close();

            System.out.println(right+ " tags are correct \n" +wrong+ " tags are incorrect");
        }

        /**
         *
         * This method allows the user to give the tags from the input line, and then runs the Viterbi Decoding using the Brown Dataset
         *
         *
         */

        public static void console_testing() throws IOException {
            Scanner in = new Scanner(System.in);
            System.out.println("We are about to do a console test. Please enter a sentence to be tested. \n");
            String sentence = in.nextLine();
            try {
                model_training("PS5/brown-train-sentences.txt", "PS5/brown-train-tags.txt");
            }
            catch (Exception e)
            {
                System.out.println("There is an issue with one of the files" +e.getMessage());
            }

            System.out.println(ViterbiDecoding(sentence));
        }


        public static void main(String[]args) throws IOException {

            //ORIGINAL TESTING WITH FILE AND SAMPLE SENTENCE
            model_training("PS5/example-sentences.txt", "PS5/example-tags.txt");
            System.out.println(ViterbiDecoding("The mine has many fish food"));

            // TEST WITH HARD CODING
            observations = new HashMap<>();
            transitions = new HashMap<>();

            Map<String, Double> tempMap = new HashMap<>();
            tempMap.put("and",Math.log(3.0/3.0));
            observations.put("cnj",tempMap);

            tempMap = new HashMap<>();
            tempMap.put("cat",Math.log(5.0/12.0));
            tempMap.put("dog",Math.log(5.0/12.0));
            tempMap.put("watch",Math.log(2.0/12.0));
            observations.put("n",tempMap);

            tempMap = new HashMap<>();
            tempMap.put("chase",Math.log(5.0/5.0));
            observations.put("np",tempMap);

            tempMap = new HashMap<>();
            tempMap.put("chase", Math.log(2.0/9.0));
            tempMap.put("get",Math.log(1.0/9.0));
            tempMap.put("watch",Math.log(6.0/9.0));
            observations.put("v", tempMap);

            //hard coding of the transitions

            tempMap = new HashMap<>();
            tempMap.put("n",Math.log(5.0/7.0));
            tempMap.put("np",Math.log(2.0/7.0));
            transitions.put("#",tempMap);

            tempMap = new HashMap<>();
            tempMap.put("n",Math.log(1.0/3.0));
            tempMap.put("np",Math.log(1.0/3.0));
            tempMap.put("v",Math.log(1.0/3.0));
            transitions.put("cnj",tempMap);

            tempMap = new HashMap<>();
            tempMap.put("cnj",Math.log(2.0/8.0));
            tempMap.put("v",Math.log(6.0/8.0));
            transitions.put("n",tempMap);

            tempMap = new HashMap<>();
            tempMap.put("v",Math.log(2.0/2.0));
            transitions.put("np",tempMap);

            tempMap = new HashMap<>();
            tempMap.put("cnj",Math.log(1.0/9.0));
            tempMap.put("n",Math.log(6.0/9.0));
            tempMap.put("np",Math.log(2.0/9.0));
            transitions.put("v",tempMap);

            System.out.println(ViterbiDecoding("chase watch dog chase watch"));
            System.out.println(ViterbiDecoding("watch dog chase cat"));
            System.out.println(ViterbiDecoding("cat and dog chase"));


            //Model training testing
              model_training("PS5/brown-train-sentences.txt", "PS5/brown-train-tags.txt");
              performance_eval("PS5/brown-test-sentences.txt", "PS5/brown-test-tags.txt");


            //CONSOLE TESTING
            console_testing();

        }
    }
