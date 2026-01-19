package com.example.service;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.example.entity.ExaminationPackage;
import com.example.entity.PhysicalExamination;
import com.example.mapper.ExaminationPackageMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 业务层方法
 */
@Service
public class ExaminationPackageService {

    private static final Logger logger = LoggerFactory.getLogger(ExaminationPackageService.class);

    @Resource
    private ExaminationPackageMapper examinationPackageMapper;

    @Resource
    PhysicalExaminationService physicalExaminationService;

    @CacheEvict(value = {"examinationPackages", "examinationPackagesPage"}, allEntries = true)
    public void add(ExaminationPackage examinationPackage) {
        try {
            logger.info("Adding examination package: {}", examinationPackage.getName());
            examinationPackageMapper.insert(examinationPackage);
            logger.info("Successfully added examination package with ID: {}", examinationPackage.getId());
        } catch (Exception e) {
            logger.error("Error adding examination package: {}", examinationPackage.getName(), e);
            throw e;
        }
    }

    @CachePut(value = "examinationPackages", key = "#examinationPackage.id")
    @CacheEvict(value = "examinationPackagesPage", allEntries = true)
    public void updateById(ExaminationPackage examinationPackage) {
        examinationPackageMapper.updateById(examinationPackage);
    }

    @CacheEvict(value = {"examinationPackages", "examinationPackagesPage"}, key = "#id", allEntries = true)
    public void deleteById(Integer id) {
        examinationPackageMapper.deleteById(id);
    }

    public void deleteBatch(List<Integer> ids) {
        for (Integer id : ids) {
            examinationPackageMapper.deleteById(id);
        }
    }

    /**
     * 根据ID查询体检套餐
     * 使用@Cacheable注解进行缓存，配合RedisConfig中的CacheErrorHandler处理缓存异常
     */
    @Cacheable(value = "examinationPackages", key = "#id")
    public ExaminationPackage selectById(Integer id) {
        try {
            logger.debug("Selecting examination package by ID: {}", id);
            ExaminationPackage examinationPackage = examinationPackageMapper.selectById(id);
            if (examinationPackage == null) {
                logger.warn("Examination package not found for ID: {}", id);
                return null;
            }
            logger.debug("Successfully found examination package: {} (ID: {})", examinationPackage.getName(), id);
            return examinationPackage;
        } catch (Exception e) {
            logger.error("Error selecting examination package by ID: {}", id, e);
            throw e;
        }
    }

    public List<ExaminationPackage> selectAll(ExaminationPackage examinationPackage) {
        return examinationPackageMapper.selectAll(examinationPackage);
    }

    public PageInfo<ExaminationPackage> selectPage(ExaminationPackage examinationPackage, Integer pageNum, Integer pageSize) {
        try {
            logger.info("Selecting examination packages page - pageNum: {}, pageSize: {}", pageNum, pageSize);
            PageHelper.startPage(pageNum, pageSize);
            List<ExaminationPackage> list = examinationPackageMapper.selectAll(examinationPackage);
            logger.info("Found {} examination packages", list.size());
            
            for (ExaminationPackage p : list) {
                List<PhysicalExamination> examinationList = new ArrayList<>(); // Move inside the loop to avoid shared reference
                String examinations = p.getExaminations();
                logger.debug("Processing package: {} with examinations: {}", p.getName(), examinations);
                
                if (examinations != null && !examinations.trim().isEmpty()) {
                    try {
                        JSONArray examinationIds = JSONUtil.parseArray(examinations);
                        logger.debug("Parsed examination IDs: {}", examinationIds);
                        
                        for (Object examinationId : examinationIds) {
                            try {
                                PhysicalExamination physicalExamination = physicalExaminationService.selectById((Integer) examinationId);
                                if (physicalExamination != null) {
                                    examinationList.add(physicalExamination);
                                    logger.debug("Added physical examination: {} (ID: {})", physicalExamination.getName(), examinationId);
                                } else {
                                    logger.warn("Physical examination not found for ID: {}", examinationId);
                                }
                            } catch (Exception e) {
                                logger.error("Error loading physical examination with ID: {}", examinationId, e);
                            }
                        }
                    } catch (Exception e) {
                        logger.error("Error parsing examinations JSON for package: {}, JSON: {}", p.getName(), examinations, e);
                    }
                } else {
                    logger.debug("No examinations found for package: {}", p.getName());
                }
                p.setExaminationList(examinationList);
                logger.debug("Set {} examinations for package: {}", examinationList.size(), p.getName());
            }
            
            PageInfo<ExaminationPackage> pageInfo = PageInfo.of(list);
            logger.info("Successfully created page info with {} total records", pageInfo.getTotal());
            return pageInfo;
        } catch (Exception e) {
            logger.error("Error in selectPage method", e);
            throw e;
        }
    }

}
