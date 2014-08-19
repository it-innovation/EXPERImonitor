/////////////////////////////////////////////////////////////////////////
//
// © University of Southampton IT Innovation Centre, 2014
//
// Copyright in this library belongs to the University of Southampton
// IT Innovation Centre of Gamma House, Enterprise Road,
// Chilworth Science Park, Southampton, SO16 7NS, UK.
//
// This software may not be used, sold, licensed, transferred, copied
// or reproduced in whole or in part in any manner or form or in or
// on any media by any person other than in accordance with the terms
// of the Licence Agreement supplied with the software, or otherwise
// without the prior written consent of the copyright owners.
//
// This software is distributed WITHOUT ANY WARRANTY, without even the
// implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
// PURPOSE, except where stated in the Licence Agreement supplied with
// the software.
//
//	Created By :			Maxim Bashevoy
//	Created Date :			2014-08-19
//	Created for Project :           Sense4us
//
/////////////////////////////////////////////////////////////////////////
package uk.ac.soton.itinnovation.ecc.service.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;

/**
 *
 */
public class Questionnaire {

    private UUID id;
    private ArrayList<Question> questions;
    private ArrayList<Answer> answers;
    private ArrayList<User> users;

    public Questionnaire() {
        this(new ArrayList<Question>());
    }

    public Questionnaire(ArrayList<Question> questions) {
        id = UUID.randomUUID();
        this.questions = questions;
        this.answers = new ArrayList<>();
        this.users = new ArrayList<>();
    }

    public UUID getId() {
        return id;
    }

    public ArrayList<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(ArrayList<Question> questions) {
        this.questions = questions;
    }

    public void addQuestion(Question q) {
        questions.add(q);
    }

    public ArrayList<Answer> getAnswers() {
        return answers;
    }

    public void setAnswers(ArrayList<Answer> answers) {
        this.answers = answers;
    }

    public void addAnswer(Answer a) {
        this.answers.add(a);
    }

    public void addUser(User u) {
        this.users.add(u);
    }

    public User getUserById(UUID id) {
        User result = null;
        for (User u : users) {
            if (u.getId().equals(id)) {
                result = u;
                break;
            }
        }
        if (result == null) {
            throw new IllegalArgumentException("Unknown user [" + id + "]");
        } else {
            return result;
        }
    }

    public Question getQuestionById(UUID id) {
        Question result = null;

        for (Question q : questions) {
            if (q.getId().equals(id)) {
                result = q;
                break;
            }
        }

        if (result == null) {
            throw new IllegalArgumentException("Unknown question [" + id + "]");
        } else {
            return result;
        }
    }

    public HashMap<Integer, Integer> getDistributionOfAnswersForQuestion(Question q) {
        HashMap<Integer, Integer> numberOfPeoplePerOption = new HashMap<>();
        numberOfPeoplePerOption.put(1, 0);
        numberOfPeoplePerOption.put(2, 0);
        numberOfPeoplePerOption.put(3, 0);
        numberOfPeoplePerOption.put(4, 0);
        numberOfPeoplePerOption.put(5, 0);

        HashMap<Integer, ArrayList<User>> peoplePerOption = new HashMap<>();
        peoplePerOption.put(1, new ArrayList<User>());
        peoplePerOption.put(2, new ArrayList<User>());
        peoplePerOption.put(3, new ArrayList<User>());
        peoplePerOption.put(4, new ArrayList<User>());
        peoplePerOption.put(5, new ArrayList<User>());

        int tempInt, selectedOption;
        for (Answer a : answers) {
            if (a.getQuestionId().equals(q.getId())) {
                selectedOption = a.getOptionSelected();
                tempInt = numberOfPeoplePerOption.get(selectedOption);
                numberOfPeoplePerOption.remove(selectedOption);
                numberOfPeoplePerOption.put(selectedOption, tempInt + 1);
                peoplePerOption.get(selectedOption).add(getUserById(a.getUserId()));
            }
        }

        ArrayList<Integer> sortedOptions = new ArrayList(numberOfPeoplePerOption.keySet());
        Collections.sort(sortedOptions);
        Iterator<Integer> it = sortedOptions.iterator();
        Integer option, numberOfPeople;
        String thePeopleAsString;
        ArrayList<User> thePeople;
//        System.out.println("Question: " + q.getName());
        while (it.hasNext()) {
            option = it.next();
            numberOfPeople = numberOfPeoplePerOption.get(option);
            thePeople = peoplePerOption.get(option);
            if (thePeople.size() > 0) {
                thePeopleAsString = " (";
                int counter = 0;
                for (User u : thePeople) {
                    thePeopleAsString += u.getName();
                    counter++;
                    if (counter < thePeople.size()) {
                        thePeopleAsString += ", ";
                    }
                }
                thePeopleAsString += ")";
            } else {
                thePeopleAsString = "";
            }

//            System.out.println("\t" + option + ": " + numberOfPeople + (numberOfPeople == 1 ? " person" : " people") + thePeopleAsString);
        }

        return numberOfPeoplePerOption;
    }

    public int getDistributionOfAnswersForOptionAndQuestion(int option, Question q) {

        int result = 0;
        for (Answer a : answers) {
            if (option == a.getOptionSelected() && a.getQuestionId().equals(q.getId())) {
                result++;
            }
        }
        return result;
    }

    public Question getQuestionByName(String questionName) {

        Question result = null;

        for (Question q : questions) {
            if (q.getName().equals(questionName)) {
                result = q;
                break;
            }
        }

        if (result == null) {
            throw new IllegalArgumentException("Unknown question name [" + questionName + "]");
        } else {
            return result;
        }

    }

    public ArrayList<User> getUsersForQuestionAndOption(int option, Question q) {
        ArrayList<User> result = new ArrayList<>();

        for (Answer a : answers) {
            if (a.getOptionSelected() == option && a.getQuestionId().equals(q.getId())) {
                result.add(getUserById(a.getUserId()));
            }
        }

        return result;
    }

    public void getAnswerDistributionForUser(User u) {
//        System.out.println("Answers for user '" + u.getName() + "': ");
        int counter = 1;
        for (Question q : questions) {
            for (Answer a : answers) {
                if (a.getUserId().equals(u.getId()) && a.getQuestionId().equals(q.getId())) {
//                    System.out.println("\tQuestion " + counter + " (" + q.getName() + "): " + a.getOptionSelected());
                    break;
                }
            }
            counter++;
        }
    }

}
