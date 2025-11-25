package com.example._207114;

import java.sql.*;
import java.io.*;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:cv_builder.db";
    private static DatabaseManager instance;
    private Connection connection;

    private DatabaseManager() {
        try {
            connection = DriverManager.getConnection(DB_URL);
            createTables();
        } catch (SQLException e) {
            System.err.println("Database connection error: " + e.getMessage());
        }
    }

    public static DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        System.out.println("db iniatia");
        return instance;
    }

    private void createTables() {
        String createResumeTable = """
            CREATE TABLE IF NOT EXISTS resumes (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                full_name TEXT NOT NULL,
                email TEXT,
                phone TEXT,
                country TEXT,
                division TEXT,
                city TEXT,
                house TEXT,
                ssc TEXT,
                hsc TEXT,
                bsc TEXT,
                msc TEXT,
                skills TEXT,
                experience TEXT,
                projects TEXT,
                image_path TEXT,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """;

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createResumeTable);
            System.out.println("Database tables created successfully");
        } catch (SQLException e) {
            System.err.println("Error creating tables: " + e.getMessage());
        }
    }

    public int saveResume(Resume resume) {
        String sql = """
        INSERT INTO resumes (full_name, email, phone, country, division, city, house,
                             ssc, hsc, bsc, msc, skills, experience, projects, image_path)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    """;

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, resume.getFullName());
            pstmt.setString(2, resume.getEmail());
            pstmt.setString(3, resume.getPhone());
            pstmt.setString(4, resume.getCountry());
            pstmt.setString(5, resume.getDivision());
            pstmt.setString(6, resume.getCity());
            pstmt.setString(7, resume.getHouse());
            pstmt.setString(8, resume.getSsc());
            pstmt.setString(9, resume.getHsc());
            pstmt.setString(10, resume.getBsc());
            pstmt.setString(11, resume.getMsc());
            pstmt.setString(12, resume.getSkills());
            pstmt.setString(13, resume.getExperience());
            pstmt.setString(14, resume.getProjects());
            pstmt.setString(15, resume.getImagePath());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (Statement stmt = connection.createStatement()) {
                    ResultSet rs = stmt.executeQuery("SELECT last_insert_rowid()");
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error saving resume: " + e.getMessage());
        }
        return -1;
    }


    public boolean updateResume(Resume resume) {
        String sql = """
            UPDATE resumes SET 
                full_name = ?, email = ?, phone = ?, country = ?, division = ?, 
                city = ?, house = ?, ssc = ?, hsc = ?, bsc = ?, msc = ?,
                skills = ?, experience = ?, projects = ?, image_path = ?,
                updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
        """;

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, resume.getFullName());
            pstmt.setString(2, resume.getEmail());
            pstmt.setString(3, resume.getPhone());
            pstmt.setString(4, resume.getCountry());
            pstmt.setString(5, resume.getDivision());
            pstmt.setString(6, resume.getCity());
            pstmt.setString(7, resume.getHouse());
            pstmt.setString(8, resume.getSsc());
            pstmt.setString(9, resume.getHsc());
            pstmt.setString(10, resume.getBsc());
            pstmt.setString(11, resume.getMsc());
            pstmt.setString(12, resume.getSkills());
            pstmt.setString(13, resume.getExperience());
            pstmt.setString(14, resume.getProjects());
            pstmt.setString(15, resume.getImagePath());
            pstmt.setInt(16, resume.getId());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating resume: " + e.getMessage());
            return false;
        }
    }

    public Resume getResumeById(int id) {
        String sql = "SELECT * FROM resumes WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return extractResumeFromResultSet(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error getting resume: " + e.getMessage());
        }
        return null;
    }

    public ResultSet getAllResumes() {
        String sql = "SELECT id, full_name, email, created_at FROM resumes ORDER BY updated_at DESC";

        try {
            Statement stmt = connection.createStatement();
            return stmt.executeQuery(sql);
        } catch (SQLException e) {
            System.err.println("Error getting all resumes: " + e.getMessage());
            return null;
        }
    }

    public boolean deleteResume(int id) {
        String sql = "DELETE FROM resumes WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting resume: " + e.getMessage());
            return false;
        }
    }

    private Resume extractResumeFromResultSet(ResultSet rs) throws SQLException {
        Resume resume = new Resume();
        resume.setId(rs.getInt("id"));
        resume.setFullName(rs.getString("full_name"));
        resume.setEmail(rs.getString("email"));
        resume.setPhone(rs.getString("phone"));
        resume.setCountry(rs.getString("country"));
        resume.setDivision(rs.getString("division"));
        resume.setCity(rs.getString("city"));
        resume.setHouse(rs.getString("house"));
        resume.setSsc(rs.getString("ssc"));
        resume.setHsc(rs.getString("hsc"));
        resume.setBsc(rs.getString("bsc"));
        resume.setMsc(rs.getString("msc"));
        resume.setSkills(rs.getString("skills"));
        resume.setExperience(rs.getString("experience"));
        resume.setProjects(rs.getString("projects"));
        resume.setImagePath(rs.getString("image_path"));
        return resume;
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }
}