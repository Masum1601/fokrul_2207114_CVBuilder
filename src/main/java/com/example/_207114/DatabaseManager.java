package com.example._207114;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:cv_builder.db";
    private static DatabaseManager instance;
    private Connection connection;

    private final ExecutorService executorService;

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    private final BlockingQueue<Connection> connectionPool;
    private static final int POOL_SIZE = 5;

    private DatabaseManager() {
        executorService = Executors.newFixedThreadPool(3);

        connectionPool = new LinkedBlockingQueue<>(POOL_SIZE);

        try {
            connection = DriverManager.getConnection(DB_URL);
            createTables();


            for (int i = 0; i < POOL_SIZE; i++) {
                connectionPool.offer(DriverManager.getConnection(DB_URL));
            }

            System.out.println("Database initialized with connection pool");
        } catch (SQLException e) {
            System.err.println("Database connection error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
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

        lock.writeLock().lock();
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createResumeTable);
            System.out.println("Database tables created successfully");
        } catch (SQLException e) {
            System.err.println("Error creating tables: " + e.getMessage());
            e.printStackTrace();
        } finally {
            lock.writeLock().unlock();
        }
    }

    private Connection getConnection() throws InterruptedException {
        return connectionPool.take();
    }

    private void returnConnection(Connection conn) {
        if (conn != null) {
            connectionPool.offer(conn);
        }
    }

    public int saveResume(Resume resume) {
        lock.writeLock().lock();
        Connection conn = null;

        try {
            conn = getConnection();
            String sql = """
                INSERT INTO resumes (full_name, email, phone, country, division, city, house,
                                    ssc, hsc, bsc, msc, skills, experience, projects, image_path)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                setResumeParameters(pstmt, resume);

                int affectedRows = pstmt.executeUpdate();
                System.out.println("Affected rows: " + affectedRows);

                if (affectedRows > 0) {
                    // SQLite-specific way to get last inserted ID
                    try (Statement stmt = conn.createStatement();
                         ResultSet rs = stmt.executeQuery("SELECT last_insert_rowid()")) {
                        if (rs.next()) {
                            int id = rs.getInt(1);
                            System.out.println("Resume saved with ID: " + id);
                            return id;
                        }
                    }
                }
            }
        } catch (SQLException | InterruptedException e) {
            System.err.println("Error saving resume: " + e.getMessage());
            e.printStackTrace();
        } finally {
            returnConnection(conn);
            lock.writeLock().unlock();
        }
        return -1;
    }

    public CompletableFuture<Integer> saveResumeAsync(Resume resume) {
        return CompletableFuture.supplyAsync(() -> saveResume(resume), executorService);
    }

    public boolean updateResume(Resume resume) {
        lock.writeLock().lock();
        Connection conn = null;

        try {
            conn = getConnection();
            String sql = """
                UPDATE resumes SET 
                    full_name = ?, email = ?, phone = ?, country = ?, division = ?, 
                    city = ?, house = ?, ssc = ?, hsc = ?, bsc = ?, msc = ?,
                    skills = ?, experience = ?, projects = ?, image_path = ?,
                    updated_at = CURRENT_TIMESTAMP
                WHERE id = ?
            """;

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                setResumeParameters(pstmt, resume);
                pstmt.setInt(16, resume.getId());

                int affectedRows = pstmt.executeUpdate();
                System.out.println("Update affected rows: " + affectedRows);
                return affectedRows > 0;
            }
        } catch (SQLException | InterruptedException e) {
            System.err.println("Error updating resume: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            returnConnection(conn);
            lock.writeLock().unlock();
        }
    }

    public CompletableFuture<Boolean> updateResumeAsync(Resume resume) {
        return CompletableFuture.supplyAsync(() -> updateResume(resume), executorService);
    }

    public Resume getResumeById(int id) {
        lock.readLock().lock();
        Connection conn = null;

        try {
            conn = getConnection();
            String sql = "SELECT * FROM resumes WHERE id = ?";

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, id);
                ResultSet rs = pstmt.executeQuery();

                if (rs.next()) {
                    Resume resume = extractResumeFromResultSet(rs);
                    System.out.println("Resume loaded: " + resume);
                    return resume;
                }
            }
        } catch (SQLException | InterruptedException e) {
            System.err.println("Error getting resume: " + e.getMessage());
            e.printStackTrace();
        } finally {
            returnConnection(conn);
            lock.readLock().unlock();
        }
        return null;
    }

    public CompletableFuture<Resume> getResumeByIdAsync(int id) {
        return CompletableFuture.supplyAsync(() -> getResumeById(id), executorService);
    }

    public List<ResumeListItem> getAllResumes() {
        lock.readLock().lock();
        Connection conn = null;
        List<ResumeListItem> resumes = new ArrayList<>();

        try {
            conn = getConnection();
            String sql = "SELECT id, full_name, email, created_at FROM resumes ORDER BY updated_at DESC";

            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {

                while (rs.next()) {
                    resumes.add(new ResumeListItem(
                            rs.getInt("id"),
                            rs.getString("full_name"),
                            rs.getString("email")
                    ));
                }
                System.out.println("Loaded " + resumes.size() + " resumes");
            }
        } catch (SQLException | InterruptedException e) {
            System.err.println("Error getting all resumes: " + e.getMessage());
            e.printStackTrace();
        } finally {
            returnConnection(conn);
            lock.readLock().unlock();
        }
        return resumes;
    }

    public CompletableFuture<List<ResumeListItem>> getAllResumesAsync() {
        return CompletableFuture.supplyAsync(this::getAllResumes, executorService);
    }

    public boolean deleteResume(int id) {
        lock.writeLock().lock();
        Connection conn = null;

        try {
            conn = getConnection();
            String sql = "DELETE FROM resumes WHERE id = ?";

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, id);
                int affectedRows = pstmt.executeUpdate();
                System.out.println("Delete affected rows: " + affectedRows);
                return affectedRows > 0;
            }
        } catch (SQLException | InterruptedException e) {
            System.err.println("Error deleting resume: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            returnConnection(conn);
            lock.writeLock().unlock();
        }
    }


    public CompletableFuture<Boolean> deleteResumeAsync(int id) {
        return CompletableFuture.supplyAsync(() -> deleteResume(id), executorService);
    }

    private void setResumeParameters(PreparedStatement pstmt, Resume resume) throws SQLException {
        pstmt.setString(1, resume.getFullName() != null ? resume.getFullName() : "");
        pstmt.setString(2, resume.getEmail() != null ? resume.getEmail() : "");
        pstmt.setString(3, resume.getPhone() != null ? resume.getPhone() : "");
        pstmt.setString(4, resume.getCountry() != null ? resume.getCountry() : "");
        pstmt.setString(5, resume.getDivision() != null ? resume.getDivision() : "");
        pstmt.setString(6, resume.getCity() != null ? resume.getCity() : "");
        pstmt.setString(7, resume.getHouse() != null ? resume.getHouse() : "");
        pstmt.setString(8, resume.getSsc() != null ? resume.getSsc() : "");
        pstmt.setString(9, resume.getHsc() != null ? resume.getHsc() : "");
        pstmt.setString(10, resume.getBsc() != null ? resume.getBsc() : "");
        pstmt.setString(11, resume.getMsc() != null ? resume.getMsc() : "");
        pstmt.setString(12, resume.getSkills() != null ? resume.getSkills() : "");
        pstmt.setString(13, resume.getExperience() != null ? resume.getExperience() : "");
        pstmt.setString(14, resume.getProjects() != null ? resume.getProjects() : "");
        pstmt.setString(15, resume.getImagePath() != null ? resume.getImagePath() : "");
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


    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
        }
        closeAllConnections();
    }

    private void closeAllConnections() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }


            Connection conn;
            while ((conn = connectionPool.poll()) != null) {
                if (!conn.isClosed()) {
                    conn.close();
                }
            }
            System.out.println("All database connections closed");
        } catch (SQLException e) {
            System.err.println("Error closing connections: " + e.getMessage());
        }
    }

    public static class ResumeListItem {
        private final int id;
        private final String fullName;
        private final String email;

        public ResumeListItem(int id, String fullName, String email) {
            this.id = id;
            this.fullName = fullName;
            this.email = email;
        }

        public int getId() {
            return id;
        }

        public String getFullName() {
            return fullName;
        }

        public String getEmail() {
            return email;
        }
    }
}