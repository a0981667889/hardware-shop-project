package com.example.starter.service;

import com.example.starter.dto.ComponentRequest;
import com.example.starter.dto.ComponentResponse;
import com.example.starter.entity.Component;
import com.example.starter.exception.ComponentNotFoundException;
import com.example.starter.repository.ComponentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ComponentService {

    private final ComponentRepository componentRepository;

    public List<ComponentResponse> findAll(String category) {
        List<Component> components = (category == null || category.isBlank())
                ? componentRepository.findAll()
                : componentRepository.findByCategory(category.toUpperCase());

        return components.stream().map(ComponentResponse::new).toList();
    }

    public ComponentResponse findById(Long id) {
        Component c = componentRepository.findById(id)
                .orElseThrow(() -> new ComponentNotFoundException(id));
        return new ComponentResponse(c);
    }

    @Transactional
    public ComponentResponse create(ComponentRequest req) {
        Component c = new Component();
        applyRequest(c, req);
        Component saved = componentRepository.save(c);
        return new ComponentResponse(saved);
    }

    @Transactional
    public ComponentResponse update(Long id, ComponentRequest req) {
        Component c = componentRepository.findById(id)
                .orElseThrow(() -> new ComponentNotFoundException(id));
        applyRequest(c, req);
        return new ComponentResponse(componentRepository.save(c));
    }

    @Transactional
    public void delete(Long id) {
        if (!componentRepository.existsById(id)) {
            throw new ComponentNotFoundException(id);
        }
        componentRepository.deleteById(id);
    }

    private void applyRequest(Component c, ComponentRequest req) {
        c.setName(req.getName());
        c.setCategory(req.getCategory().toUpperCase());
        c.setBrand(req.getBrand());
        c.setPrice(req.getPrice());
        c.setStock(req.getStock());
        c.setSocket(req.getSocket());
        c.setPowerWatt(req.getPowerWatt());
    }
}