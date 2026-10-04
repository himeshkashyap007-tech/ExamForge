package com.examforge.dao;

import com.examforge.exception.DatabaseException;
import com.examforge.model.ExaminationRecord;
import com.examforge.model.Paper;
import com.examforge.model.PaperQuestion;
import com.examforge.model.PaperStatus;
import java.util.List;

public interface PaperDAO {
    Paper findById(int id) throws DatabaseException;
    List<Paper> findAll() throws DatabaseException;
    List<Paper> findByStatus(PaperStatus status) throws DatabaseException;
    List<Paper> findByBlueprintId(int blueprintId) throws DatabaseException;
    List<Paper> findByCreatedBy(int userId) throws DatabaseException;
    int insert(Paper paper) throws DatabaseException;
    boolean update(Paper paper) throws DatabaseException;
    boolean delete(int paperId) throws DatabaseException;
    boolean updateStatus(int paperId, PaperStatus status) throws DatabaseException;

    List<PaperQuestion> findQuestionsByPaperId(int paperId) throws DatabaseException;
    int insertPaperQuestion(PaperQuestion pq) throws DatabaseException;
    boolean deletePaperQuestionsByPaperId(int paperId) throws DatabaseException;

    int insertExaminationRecord(ExaminationRecord record) throws DatabaseException;
    List<ExaminationRecord> findAllExaminationRecords() throws DatabaseException;
    List<ExaminationRecord> findBySubject(String subject) throws DatabaseException;
    ExaminationRecord findExaminationByPaperId(int paperId) throws DatabaseException;
}
