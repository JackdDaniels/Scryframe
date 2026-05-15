package jorgedediego.scryframe.infrastructure.rest;

import jorgedediego.scryframe.application.dto.TagDTO;
import jorgedediego.scryframe.application.services.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/tags")
@CrossOrigin(origins = "http://localhost:4200")
public class TagController {
    
    @Autowired
    private TagService tagService;

    @GetMapping("/list")
    public List<TagDTO> getTags(){
        return tagService.getTags();
    }

    @GetMapping("/{id}")
    public Optional<TagDTO> getTagById(
            @PathVariable String id
    ){
        return tagService.getTagById(id) ;
    }

    @PostMapping("")
    public String saveTag(
            @RequestBody TagDTO tag
    ){
        return tagService.saveTag( tag );
    }

    @DeleteMapping("/{id}")
    public String deleteTag(
            @PathVariable String id
    ){
        return tagService.deleteTag( id );
    }
}
