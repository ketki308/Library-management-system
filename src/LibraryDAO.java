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
public boolean returnBook(int bookId) {
    try {
        Connection con = DBConnection.getConnection();

        String sql =
            "UPDATE bookstable " +
            "SET AVAILABLE_COPIES = AVAILABLE_COPIES + 1 " +
            "WHERE BOOK_ID = ?";

        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, bookId);

        return ps.executeUpdate() > 0;

    } catch (Exception e) {
        e.printStackTrace();
        return false;
    }
    }
public boolean addBook(int bookId, String category, String name, String author, int copies) {
    try {
        Connection con = DBConnection.getConnection();

        String sql =
            "INSERT INTO bookstable (BOOK_ID, CATEGORY, NAME, AUTHOR, COPIES, AVAILABLE_COPIES) " +
            "VALUES (?, ?, ?, ?, ?, ?)";

        PreparedStatement pst = con.prepareStatement(sql);
        pst.setInt(1, bookId);
        pst.setString(2, category);
        pst.setString(3, name);
        pst.setString(4, author);
        pst.setInt(5, copies);
        pst.setInt(6, copies); // initially available = total copies

        int rows = pst.executeUpdate();
        pst.close();

        return rows > 0;

    } catch (Exception e) {
        e.printStackTrace();
    }
    return false;
}

}
