package com.examforge.dao;

import com.examforge.exception.DatabaseException;
import com.examforge.model.Blueprint;
import com.examforge.model.BlueprintConstraint;
import com.examforge.model.BlueprintSection;
import com.examforge.model.BlueprintStatus;
import java.util.List;

public interface BlueprintDAO {
    Blueprint findById(int id) throws DatabaseException;
    List<Blueprint> findAll() throws DatabaseException;
    List<Blueprint> findByStatus(BlueprintStatus status) throws DatabaseException;
    List<Blueprint> findBySubject(String subject) throws DatabaseException;
    List<Blueprint> findByCreatedBy(int userId) throws DatabaseException;
    int insert(Blueprint blueprint) throws DatabaseException;
    boolean update(Blueprint blueprint) throws DatabaseException;
    boolean delete(int blueprintId) throws DatabaseException;
    boolean updateStatus(int blueprintId, BlueprintStatus status) throws DatabaseException;
    boolean deactivate(int blueprintId) throws DatabaseException;
    boolean reactivate(int blueprintId) throws DatabaseException;
    int countSectionsByBlueprintId(int blueprintId) throws DatabaseException;

    List<BlueprintSection> findSectionsByBlueprintId(int blueprintId) throws DatabaseException;
    int insertSection(BlueprintSection section) throws DatabaseException;
    boolean updateSection(BlueprintSection section) throws DatabaseException;
    boolean deleteSectionsByBlueprintId(int blueprintId) throws DatabaseException;

    List<BlueprintConstraint> findConstraintsByBlueprintId(int blueprintId) throws DatabaseException;
    int insertConstraint(BlueprintConstraint constraint) throws DatabaseException;
    boolean updateConstraint(BlueprintConstraint constraint) throws DatabaseException;
    boolean deleteConstraintsByBlueprintId(int blueprintId) throws DatabaseException;
}
