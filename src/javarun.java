public class javarun{
public static void main(String[] args) {
    LibraryDAO dao = new LibraryDAO();
    boolean success=dao.issueBook(1,101);
    boolean reserved=dao.reserveBook(1,102);
   


    System.out.println(reserved);
    System.out.println(success);
    System.out.println(dao.getAvailableCopies(1));
}
}

