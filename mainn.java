public class mainn{
public static void main(String[] args) {
    LibraryDAO dao = new LibraryDAO();
    System.out.println(dao.getAvailableCopies(1));
}
}

