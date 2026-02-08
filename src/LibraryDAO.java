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
    try {
        Connection con = DBConnection.getConnection();

        // Step 1: check availability
        if (getAvailableCopies(bookId) <= 0) {
            return false;
        }

        // Step 2: insert issue record
        String issueSql =
            "INSERT INTO Issued_Books (BOOK_ID, STUDENT_ID, ISSUE_DATE, STATUS) " +
            "VALUES (?, ?, CURDATE(), 'ISSUED')";

        PreparedStatement ps1 = con.prepareStatement(issueSql);
        ps1.setInt(1, bookId);
        ps1.setInt(2, studentId);
        ps1.executeUpdate();

        // Step 3: decrease available copies
        String updateSql =
            "UPDATE bookstable SET AVAILABLE_COPIES = AVAILABLE_COPIES - 1 " +
            "WHERE BOOK_ID = ?";

        PreparedStatement ps2 = con.prepareStatement(updateSql);
        ps2.setInt(1, bookId);
        ps2.executeUpdate();

        return true;

    } catch (Exception e) {
        e.printStackTrace();
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
}
