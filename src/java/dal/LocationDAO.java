package dal;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.sql.Connection;
import java.util.ArrayList;
import model.Location;

public class LocationDAO {

    Location mapLocation(ResultSet rs) throws SQLException {
        return new Location(rs.getInt("id"), rs.getString("name"));
    }

    public List<Location> getAll() throws SQLException {

        String sql = "SELECT * FROM locations";

        List<Location> locations = new ArrayList<>();

        try (
                Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                locations.add(mapLocation(rs));
            }

        }
        return locations;
    }
}
