package com.examforge.service;

import com.examforge.dao.BlueprintDAO;
import com.examforge.dao.PaperDAO;
import com.examforge.dao.impl.BlueprintDAOImpl;
import com.examforge.dao.impl.PaperDAOImpl;
import com.examforge.exception.DatabaseException;
import com.examforge.exception.ExamForgeException;
import com.examforge.model.*;
import com.examforge.validator.BlueprintValidator;

import java.util.List;

public class BlueprintService {
    private final BlueprintDAO blueprintDAO;
    private final PaperDAO paperDAO;
    private final BlueprintValidator blueprintValidator;

    public BlueprintService() {
        this.blueprintDAO = new BlueprintDAOImpl();
        this.paperDAO = new PaperDAOImpl();
        this.blueprintValidator = new BlueprintValidator();
    }

    public Blueprint createBlueprint(Blueprint blueprint) throws ExamForgeException {
        if (blueprint.getExamName() == null || blueprint.getExamName().trim().isEmpty()) {
            throw new ExamForgeException("Exam name is required");
        }
        if (blueprint.getSubject() == null || blueprint.getSubject().trim().isEmpty()) {
            throw new ExamForgeException("Subject is required");
        }
        if (blueprint.getTotalMarks() <= 0) {
            throw new ExamForgeException("Total marks must be positive");
        }
        if (blueprint.getNumberOfQuestions() <= 0) {
            throw new ExamForgeException("Number of questions must be positive");
        }
        try {
            blueprint.setStatus(BlueprintStatus.DRAFT);
            int id = blueprintDAO.insert(blueprint);
            blueprint.setBlueprintId(id);
            for (BlueprintSection section : blueprint.getSections()) {
                section.setBlueprintId(id);
                int sectionId = blueprintDAO.insertSection(section);
                section.setSectionId(sectionId);
            }
            for (BlueprintConstraint constraint : blueprint.getConstraints()) {
                constraint.setBlueprintId(id);
                int constraintId = blueprintDAO.insertConstraint(constraint);
                constraint.setConstraintId(constraintId);
            }
            return blueprint;
        } catch (DatabaseException e) {
            throw new ExamForgeException("Failed to create blueprint: " + e.getMessage(), e);
        }
    }

    public boolean updateBlueprint(Blueprint blueprint) throws ExamForgeException {
        try {
            blueprintDAO.deleteSectionsByBlueprintId(blueprint.getBlueprintId());
            blueprintDAO.deleteConstraintsByBlueprintId(blueprint.getBlueprintId());
            blueprintDAO.update(blueprint);
            for (BlueprintSection section : blueprint.getSections()) {
                section.setBlueprintId(blueprint.getBlueprintId());
                if (section.getSectionId() > 0) {
                    blueprintDAO.updateSection(section);
                } else {
                    int sectionId = blueprintDAO.insertSection(section);
                    section.setSectionId(sectionId);
                }
            }
            for (BlueprintConstraint constraint : blueprint.getConstraints()) {
                constraint.setBlueprintId(blueprint.getBlueprintId());
                if (constraint.getConstraintId() > 0) {
                    blueprintDAO.updateConstraint(constraint);
                } else {
                    int constraintId = blueprintDAO.insertConstraint(constraint);
                    constraint.setConstraintId(constraintId);
                }
            }
            return true;
        } catch (DatabaseException e) {
            throw new ExamForgeException("Failed to update blueprint: " + e.getMessage(), e);
        }
    }

    public Blueprint validateBlueprint(Blueprint blueprint) throws ExamForgeException {
        List<String> errors = blueprintValidator.validate(blueprint);
        if (errors.isEmpty()) {
            blueprint.setStatus(BlueprintStatus.VALID);
        } else {
            blueprint.setStatus(BlueprintStatus.INVALID);
        }
        try {
            blueprintDAO.updateStatus(blueprint.getBlueprintId(), blueprint.getStatus());
            return blueprint;
        } catch (DatabaseException e) {
            throw new ExamForgeException("Failed to validate blueprint: " + e.getMessage(), e);
        }
    }

    public boolean deleteBlueprint(int blueprintId) throws ExamForgeException {
        try {
            List<com.examforge.model.Paper> papers = paperDAO.findByBlueprintId(blueprintId);
            if (papers != null && !papers.isEmpty()) {
                throw new ExamForgeException("Cannot delete blueprint: it is used by " + papers.size() +
                                             " paper(s). Delete or reassign those papers first.");
            }
            return blueprintDAO.delete(blueprintId);
        } catch (DatabaseException e) {
            throw new ExamForgeException("Failed to delete blueprint: " + e.getMessage(), e);
        }
    }

    public void deactivateBlueprint(int blueprintId) throws ExamForgeException {
        try {
            blueprintDAO.deactivate(blueprintId);
        } catch (DatabaseException e) {
            throw new ExamForgeException("Failed to deactivate blueprint: " + e.getMessage(), e);
        }
    }

    public boolean reactivateBlueprint(int blueprintId) throws ExamForgeException {
        try {
            return blueprintDAO.reactivate(blueprintId);
        } catch (DatabaseException e) {
            throw new ExamForgeException("Failed to reactivate blueprint: " + e.getMessage(), e);
        }
    }

    public int getSectionCount(int blueprintId) throws ExamForgeException {
        try {
            return blueprintDAO.countSectionsByBlueprintId(blueprintId);
        } catch (DatabaseException e) {
            throw new ExamForgeException("Error counting blueprint sections: " + e.getMessage(), e);
        }
    }

    public Blueprint findById(int id) throws ExamForgeException {
        try {
            return blueprintDAO.findById(id);
        } catch (DatabaseException e) {
            throw new ExamForgeException("Error finding blueprint: " + e.getMessage(), e);
        }
    }

    public List<Blueprint> findAll() throws ExamForgeException {
        try {
            return blueprintDAO.findAll();
        } catch (DatabaseException e) {
            throw new ExamForgeException("Error listing blueprints: " + e.getMessage(), e);
        }
    }

    public List<Blueprint> findBySubject(String subject) throws ExamForgeException {
        try {
            return blueprintDAO.findBySubject(subject);
        } catch (DatabaseException e) {
            throw new ExamForgeException("Error listing blueprints: " + e.getMessage(), e);
        }
    }

    public List<String> validateBlueprintStructure(Blueprint blueprint) {
        return blueprintValidator.validate(blueprint);
    }
}
