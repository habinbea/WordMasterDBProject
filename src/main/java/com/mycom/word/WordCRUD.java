package com.mycom.word;

import java.io.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Scanner;

public class WordCRUD implements ICRUD{
    final String WORD_SELECTALL = "select * from dictionary";
    final String WORD_SELECT = "select * from dictionary where word like ? ";

    final String WORD_INSERT = "insert into dictionary (level, word, meaning, regdate) "
            + "values (?,?,?,?) ";
    final String WORD_UPDATE = "update dictionary set meaning=? where id=? ";
    final String WORD_DELETE = "delete from dictionary where id=? ";

    ArrayList<Word> list;
    Scanner s;
    final String fname = "Dictionary.txt";
    Connection conn;

    WordCRUD(Scanner s){
        list = new ArrayList<>();
        this.s = s;
        conn = DBConnection.getConnection();
    }

    public void loadData(String keyword){
        list.clear();
        try {
            PreparedStatement stmt = conn.prepareStatement(WORD_SELECTALL);
            ResultSet rs;

            if(keyword.equals("")){
                stmt = conn.prepareStatement(WORD_SELECTALL);
                rs = stmt.executeQuery();
            } else {
                stmt = conn.prepareStatement(WORD_SELECT);
                stmt.setString(1, "%" + keyword + "%");
                rs = stmt.executeQuery();
            }
            while(true){
                if(!rs.next()) break;
                int id = rs.getInt("id");
                int level = rs.getInt("level");
                String word = rs.getString("word");
                String meaning = rs.getString("meaning");
                list.add(new Word(id, level, word, meaning));
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public String getCurrentDate(){
        LocalDate now = LocalDate.now();
        DateTimeFormatter f = DateTimeFormatter.ofPattern("yyy-MM-dd");
        return f.format(now);
    }

    @Override
    public int add(Word one) {
        int retval = 0;
        PreparedStatement pstmt;
        try {
            pstmt = conn.prepareStatement(WORD_INSERT);
            pstmt.setInt(1, one.getLevel());
            pstmt.setString(2, one.getWord());
            pstmt.setString(3, one.getMeaning());
            pstmt.setString(4, getCurrentDate());
            retval = pstmt.executeUpdate();
            pstmt.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return 0;
    }

    public void addItem() {
        System.out.print("=> 난이도(1,2,3) & 새 단어 입력 : ");
        int level = s.nextInt();
        String word = s.nextLine();

        System.out.print("뜻 입력 : ");
        String meaning = s.nextLine();

        Word one = new Word(0, level, word, meaning);
        int retval = add(one);

        if(retval > 0) System.out.println("새 단어가 단어장에 추가되었습니다. ");
        else System.out.println("새 단어 추가중 에러가 발생되었습니다. ");
    }

    public void listAll(String keyword) {
        loadData(keyword);
        System.out.println("----------------------");
        for(int i = 0; i < list.size(); i++) {
            System.out.print(i+1 + " ");
            System.out.println(list.get(i).toString());
        }
        System.out.println("----------------------");
    }

    public void listAll(int level){

        int j = 0;
        System.out.println("----------------------");
        for(int i = 0; i < list.size(); i++) {
            int ilevel = list.get(i).getLevel();
            if(ilevel != level) continue;
            System.out.print(j+1 + " ");
            System.out.println(list.get(i).toString());
            j++;
        }
        System.out.println("----------------------");
    }

    @Override
    public int update(Word one) {
        int retval = 0;
        PreparedStatement pstmt;
        try {
            pstmt = conn.prepareStatement(WORD_UPDATE);
            pstmt.setString(1, one.getMeaning());
            pstmt.setInt(2, one.getId());
            retval = pstmt.executeUpdate();
            pstmt.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return retval;
    }

    public void updateItem(){
        System.out.print("=> 수정할 단어 검색 : ");
        String keyword = s.next();
        listAll(keyword);

        System.out.print("=> 수정할 번호 선택 : ");
        int id = s.nextInt();
        s.nextLine();

        System.out.print("=> 뜻 입력 : ");
        String meaning = s.nextLine();

        int val = update(new Word(list.get(id-1).getId(), 0, "", meaning));
        if(val > 0)
            System.out.println("단어가 수정되었습니다. ");
        else
            System.out.println("[수정] 에러발생 !!!");
    }

    @Override
    public int delete(Word one) {
        int retval = 0;
        PreparedStatement pstmt;
        try {
            pstmt = conn.prepareStatement(WORD_DELETE);
            pstmt.setInt(1, one.getId());
            retval = pstmt.executeUpdate();
            pstmt.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return retval;
    }

    public void deleteItem(){
        System.out.print("=> 삭제할 단어 검색 : ");
        String keyword = s.next();
        listAll(keyword);
        System.out.print("=> 삭제할 번호 선택 : ");
        int id = s.nextInt();
        s.nextLine();

        System.out.print("=> 정말로 삭제하실래요?(Y/n) ");
        String ans = s.next();
        if(ans.equalsIgnoreCase("y")){
            int val = delete(new Word(list.get(id-1).getId(), 0, "", ""));
            if(val > 0) {
                //list.remove((int) idlist.get(id - 1));
                System.out.println("단어가 삭제되었습니다. ");
            } else
                System.out.println("[삭제] 에러발생 !!!");
        } else
            System.out.println("취소되었습니다. ");
    }

    public void saveFile(){
        try {
            PrintWriter pr = new PrintWriter(new FileWriter(fname));
            for(Word one : list){
                pr.write(one.toFileString() + "\n");
            }
            pr.close();
            System.out.println("==> 데이터 저장 완료!!!");

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void searchLevel(){
        System.out.print("=> 원하는 레벨은? (1~3) ");
        int level = s.nextInt();
        listAll(level);
    }

    public void searchWord(){
        System.out.print("=> 원하는 단어는? ");
        String keyword = s.next();
        listAll(keyword);
    }
}