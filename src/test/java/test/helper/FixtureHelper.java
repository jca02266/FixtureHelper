package test.helper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.Reader;
import java.sql.Connection;
import java.util.Date;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.util.StringUtils;

import au.com.bytecode.opencsv.bean.CsvToBean;
import au.com.bytecode.opencsv.bean.HeaderColumnNameMappingStrategy;

public class FixtureHelper {
    DataSource dataSource;

    public FixtureHelper(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void executeScript(Resource resource) {
        ResourceDatabasePopulator rdp = new ResourceDatabasePopulator();
        rdp.addScript(resource);
        rdp.setSqlScriptEncoding("MS932");
        rdp.setIgnoreFailedDrops(true);
        rdp.setContinueOnError(false);

        Connection conn = DataSourceUtils.getConnection(dataSource);
        rdp.populate(conn);
    }

    public void executeScript(String resourcePath) {
        Resource resource = new ClassPathResource(resourcePath);
        executeScript(resource);
    }

    public void executeScript(File file) {
        Resource resource = new FileSystemResource(file);
        executeScript(resource);
    }

    public <T> List<T> getBeans(Class<T> clazz, Resource resource) throws IOException {
        HeaderColumnNameMappingStrategy strategy = new HeaderColumnNameMappingStrategy();
        strategy.setType(clazz);

        CsvToBean csv = new CsvToBean();

        try (Reader reader = new InputStreamReader(resource.getInputStream(), "UTF-8")) {
            return csv.parse(strategy, reader);
        }
    }

    public <T> List<T> getBeans(Class<T> clazz, String csvPath) throws IOException {
        Resource resource = new ClassPathResource(csvPath, getClass());

        return getBeans(clazz, resource);
    }

    public <T> List<T> getBeans(Class<T> clazz, File file) throws IOException {
        Resource resource = new FileSystemResource(file);

        return getBeans(clazz, resource);
    }

    public void insert(Resource resource, List beans) throws IOException {
        String script;
        try (LineNumberReader reader = new LineNumberReader(new InputStreamReader(resource.getInputStream()))) {
            script = ScriptUtils.readScript(reader, "--", ";");
        }

        NamedParameterJdbcTemplate npjt = new NamedParameterJdbcTemplate(dataSource);
        for (Object bean : beans) {
            npjt.update(script, new BeanPropertySqlParameterSource(bean));
        }
    }

    public void insert(String sqlPath, List beans) throws IOException {
        Resource resource = new ClassPathResource(sqlPath, getClass());

        insert(resource, beans);
    }

    void loadFile(File csvFile) throws IOException, ClassNotFoundException {
        String fileName = csvFile.getName();
        String tableName = StringUtils.stripFilenameExtension(fileName);

        Class clazz = Class.forName("jp.sample." + tableName);
        List beans = getBeans(clazz, csvFile);

        Resource sqlCreateScript = new ClassPathResource("/sql/ddl/" + tableName + ".sql", getClass());
        Resource sqlInsertScript = new ClassPathResource("/sql/dml/" + tableName + ".sql", getClass());

        executeScript(sqlCreateScript);

        if (!sqlInsertScript.exists()) {
            // generate a SQL insert statement from bean
            String sqlString = SQLBuilder.buildInsert(tableName, beans.get(0));
            sqlInsertScript = new ByteArrayResource(sqlString.getBytes());
        }
        insert(sqlInsertScript, beans);
    }

    private void loadParentFiles(File dir) throws IOException, ClassNotFoundException {
        File parent = dir.getParentFile();
        boolean top = parent.getName().equalsIgnoreCase("fixtures");

        if (!top) {
            loadParentFiles(parent);
        }

        for (File file : parent.listFiles()) {
            if (file.isFile()) {
                if (file.getName().toLowerCase().startsWith("expect")) {
                    continue;
                }

                if (!"csv".equalsIgnoreCase(StringUtils.getFilenameExtension(file.getName()))) {
                    continue;
                }

                loadFile(file);
            }
        }

        if (top) {
            return;
        }
    }

    private void loadChildFiles(File dir) throws IOException, ClassNotFoundException {
        for (File file : dir.listFiles()) {
            if (file.isDirectory()) {
                loadChildFiles(file);
            } else {
                if (file.getName().toLowerCase().startsWith("expect")) {
                    continue;
                }

                if (!"csv".equalsIgnoreCase(StringUtils.getFilenameExtension(file.getName()))) {
                    continue;
                }

                loadFile(file);
            }
        }
    }

    private File searchPath(File dir, String patternName) {
        for (File file : dir.listFiles()) {
            if (file.isDirectory()) {
                if (patternName.equalsIgnoreCase(file.getName())) {
                    return file;
                }
                File ret = searchPath(file, patternName);
                if (ret != null) {
                    return ret;
                }
            }
        }

        return null;
    }

    public void loadFixtures(String patternName) throws IOException, ClassNotFoundException {
        Resource resource = new ClassPathResource("/fixtures", getClass());

        File dir = searchPath(resource.getFile(), patternName);
        if (dir == null) {
            throw new FileNotFoundException("directory \"" + patternName + "\" does not exists in /fixtures");
        }

        loadParentFiles(dir);
        loadChildFiles(dir);
    }

    private <T> List<T> loadBeans(Class<T> clazz, File dir, String fileName) throws IOException {
        fileName = fileName + ".csv";

        for (File file : dir.listFiles()) {
            if (file.isFile()) {
                if (fileName.equalsIgnoreCase(file.getName())) {
                    return getBeans(clazz, file);
                }
            }
        }

        throw new FileNotFoundException("file \"" + fileName + "\" does not exist in \"" + dir.getPath());
    }

    public <T> List<T> getBeans(Class<T> clazz, String patternName, String filename) throws IOException {
        Resource resource = new ClassPathResource("/fixtures", getClass());
        File dir = searchPath(resource.getFile(), patternName);
        if (dir == null) {
            throw new FileNotFoundException("directory \"" + patternName + "\" does not exists in /fixtures");
        }

        return loadBeans(clazz, dir, filename);
    }

}
