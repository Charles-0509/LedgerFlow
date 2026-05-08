package com.dailyfinance.service;

import com.dailyfinance.common.BusinessException;
import com.dailyfinance.common.PageResult;
import com.dailyfinance.dto.RecordRequest;
import com.dailyfinance.mapper.AccountMapper;
import com.dailyfinance.mapper.CategoryMapper;
import com.dailyfinance.mapper.RecordMapper;
import com.dailyfinance.model.Account;
import com.dailyfinance.model.Category;
import com.dailyfinance.model.RecordView;
import com.dailyfinance.model.TransactionRecord;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class RecordService {
    private final RecordMapper recordMapper;
    private final CategoryMapper categoryMapper;
    private final AccountMapper accountMapper;

    public RecordService(RecordMapper recordMapper, CategoryMapper categoryMapper, AccountMapper accountMapper) {
        this.recordMapper = recordMapper;
        this.categoryMapper = categoryMapper;
        this.accountMapper = accountMapper;
    }

    public PageResult<RecordView> search(Long userId,
                                         String type,
                                         Long categoryId,
                                         LocalDate startDate,
                                         LocalDate endDate,
                                         String keyword,
                                         int page,
                                         int size) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(size, 1), 100);
        String normalizedType = normalizeTypeNullable(type);
        int offset = (safePage - 1) * safeSize;
        long total = recordMapper.count(userId, normalizedType, categoryId, startDate, endDate, clean(keyword));
        List<RecordView> records = recordMapper.search(userId, normalizedType, categoryId, startDate, endDate, clean(keyword), offset, safeSize);
        return new PageResult<>(total, safePage, safeSize, records);
    }

    public RecordView detail(Long userId, Long id) {
        RecordView record = recordMapper.findViewById(id, userId);
        if (record == null) {
            throw new BusinessException("账单不存在");
        }
        return record;
    }

    public RecordView create(Long userId, RecordRequest request) {
        TransactionRecord record = buildRecord(userId, null, request);
        recordMapper.insert(record);
        return detail(userId, record.id);
    }

    public RecordView update(Long userId, Long id, RecordRequest request) {
        detail(userId, id);
        TransactionRecord record = buildRecord(userId, id, request);
        recordMapper.update(record);
        return detail(userId, id);
    }

    public void delete(Long userId, Long id) {
        if (recordMapper.softDelete(id, userId) == 0) {
            throw new BusinessException("账单不存在");
        }
    }

    private TransactionRecord buildRecord(Long userId, Long id, RecordRequest request) {
        String type = normalizeType(request.type);
        Category category = categoryMapper.findById(request.categoryId, userId);
        if (category == null) {
            throw new BusinessException("分类不存在");
        }
        if (!type.equals(category.type)) {
            throw new BusinessException("账单类型与分类类型不一致");
        }

        Long accountId = request.accountId;
        if (accountId == null) {
            Account preferred = accountMapper.findPreferred(userId);
            if (preferred == null) {
                throw new BusinessException("请先创建账户");
            }
            accountId = preferred.id;
        } else if (accountMapper.findById(accountId, userId) == null) {
            throw new BusinessException("账户不存在");
        }

        TransactionRecord record = new TransactionRecord();
        record.id = id;
        record.userId = userId;
        record.type = type;
        record.amount = request.amount;
        record.categoryId = request.categoryId;
        record.accountId = accountId;
        record.recordDate = request.recordDate;
        record.note = request.note == null || request.note.isBlank() ? null : request.note.trim();
        return record;
    }

    private String normalizeTypeNullable(String type) {
        return type == null || type.isBlank() ? null : normalizeType(type);
    }

    private String normalizeType(String type) {
        String value = type == null ? "" : type.trim().toUpperCase();
        if (!"INCOME".equals(value) && !"EXPENSE".equals(value)) {
            throw new BusinessException("账单类型必须为 INCOME 或 EXPENSE");
        }
        return value;
    }

    private String clean(String keyword) {
        return keyword == null || keyword.isBlank() ? null : keyword.trim();
    }
}
