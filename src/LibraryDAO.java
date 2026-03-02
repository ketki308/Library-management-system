import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class LibraryDAO {

    public int getAvailableCopies(int bookId) {
        int copies = -1;

        try {
            Connection con = DBConnection.getConnection();

            String sql = "SELECT AVAILABLE_COPIES FROM bookstable WHERE BOOK_ID = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, bookId);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                copies = rs.getInt("AVAILABLE_COPIES");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return copies;
    }
    public boolean issueBook(int bookId, int studentId) {
    Connection con = null;
    try {
        con = DBConnection.getConnection();
        con.setAutoCommit(false);

        // Validate student
        String checkStudent =
            "SELECT USER_ID FROM users WHERE USER_ID=? AND ROLE='STUDENT'";
        PreparedStatement psCheck = con.prepareStatement(checkStudent);
        psCheck.setInt(1, studentId);
        ResultSet rs = psCheck.executeQuery();

        if (!rs.next()) {
            con.rollback();
            return false;
        }
        
        // Step 1: decrease available copies only if > 0
        String updateBook =
            "UPDATE bookstable " +
            "SET AVAILABLE_COPIES = AVAILABLE_COPIES - 1 " +
            "WHERE BOOK_ID = ? AND AVAILABLE_COPIES > 0";

        PreparedStatement ps1 = con.prepareStatement(updateBook);
        ps1.setInt(1, bookId);

        int rows = ps1.executeUpdate();
        if (rows == 0) {
            con.rollback();
            return false;
        }

        // Step 2: insert issue record
        String issueSql =
            "INSERT INTO issued_books (BOOK_ID, STUDENT_ID, ISSUE_DATE) " +
            "VALUES (?, ?, CURDATE())";

        PreparedStatement ps2 = con.prepareStatement(issueSql);
        ps2.setInt(1, bookId);
        ps2.setInt(2, studentId);
        ps2.executeUpdate();

        con.commit();
        return true;

    } catch (Exception e) {
        try {
            if (con != null) con.rollback();
        } catch (Exception ex) {}
        e.printStackTrace();
    } finally {
        try {
            if (con != null) con.setAutoCommit(true);
        } catch (Exception e) {}
    }
    return false;
}


    public boolean reserveBook(int bookId, int studentId) {
    try {
        Connection con = DBConnection.getConnection();

        // Reservation allowed ONLY if no copies available
        if (getAvailableCopies(bookId) > 0) {
            return false;
        }

        String sql =
            "INSERT INTO reservation (BOOK_ID, STUDENT_ID, RESERVATION_DATE, STATUS) " +
            "VALUES (?, ?, CURDATE(), 'ACTIVE')";

        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, bookId);
        ps.setInt(2, studentId);
        ps.executeUpdate();

        return true;

    } catch (Exception e) {
        e.printStackTrace();
    }
    return false;
}
public boolean returnBook(int bookId, int studentId) {
    Connection con=null;
    try {
        con = DBConnection.getConnection();
        con.setAutoCommit(false);
        
        String checkStudent =
            "SELECT USER_ID FROM users WHERE USER_ID=? AND ROLE='STUDENT'";
        PreparedStatement psCheck = con.prepareStatement(checkStudent);
        psCheck.setInt(1, studentId);
        ResultSet rs = psCheck.executeQuery();

        if (!rs.next()) {
            con.rollback();
            return false;
        }
        
        // 2️⃣ Check if this student actually issued this book
        String checkIssue =
            "SELECT ISSUE_ID FROM issued_books " +
            "WHERE BOOK_ID=? AND STUDENT_ID=? AND RETURN_DATE IS NULL";

        PreparedStatement psCheckIssue = con.prepareStatement(checkIssue);
        psCheckIssue.setInt(1, bookId);
        psCheckIssue.setInt(2, studentId);

        ResultSet rsIssue = psCheckIssue.executeQuery();

        if (!rsIssue.next()) {
            con.rollback(); // student never had this book
            return false;
        }
        
         // 3️⃣ Mark book as returned
        String updateReturn =
            "UPDATE issued_books SET RETURN_DATE = CURDATE() " +
            "WHERE BOOK_ID=? AND STUDENT_ID=? AND RETURN_DATE IS NULL";

        PreparedStatement psReturn = con.prepareStatement(updateReturn);
        psReturn.setInt(1, bookId);
        psReturn.setInt(2, studentId);
        psReturn.executeUpdate();

        // 4️⃣ Increase available copies (NOW it is valid)
        String updateCopies =
            "UPDATE bookstable SET AVAILABLE_COPIES = AVAILABLE_COPIES + 1 " +
            "WHERE BOOK_ID=?";

        PreparedStatement psCopies = con.prepareStatement(updateCopies);
        psCopies.setInt(1, bookId);
        psCopies.executeUpdate();

        con.commit();
        return true;

    
    } catch (Exception e) {
        try { if (con != null) con.rollback(); } catch (Exception ex) {}
        e.printStackTrace();
    } finally {
        try { if (con != null) con.setAutoCommit(true); } catch (Exception e) {}
    }

    return false;
}
public boolean addBook(String category, String name, String author, int copies,int available) {
    try {
        Connection con = DBConnection.getConnection();

        String sql =
            "INSERT INTO bookstable (CATEGORY, NAME, AUTHOR, TOTAL_COPIES, AVAILABLE_COPIES) " +
            "VALUES (?, ?, ?, ?, ?)";

        PreparedStatement pst = con.prepareStatement(sql);
        pst.setString(1, category);
        pst.setString(2, name);
        pst.setString(3, author);
        pst.setInt(4, copies);
        pst.setInt(5, copies);

        return pst.executeUpdate() > 0;

    } catch (Exception e) {
        e.printStackTrace();
    }
    return false;
}
}
