package jorgedediego.scryframe.application.services;

import jorgedediego.scryframe.application.dto.TagDTO;
import jorgedediego.scryframe.domain.repository.TagRepository;
import jorgedediego.scryframe.domain.valueObjects.TagId;
import jorgedediego.scryframe.infrastructure.persistence.entities.TagEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TagService {
    @Autowired
    TagRepository tagRepository;

    // GET list of all tags
    public List<TagDTO> getTags(){
        return tagRepository.findAll()
                .stream()
                .map(GeneralUtilsMapper::toDTO)
                .toList();
    }

    //GET By Id
    public Optional<TagDTO> getTagById(String id){
        return tagRepository.findById(TagId.builder().id( id ).build())
                .map(GeneralUtilsMapper::toDTO);
    }

    // CREATE / SAVE
    public String saveTag(TagDTO tagDTO) {
        TagEntity savedTag = tagRepository.save(
                GeneralUtilsMapper.toDomain(tagDTO)
        );
        return "Tag saved with id: " + savedTag.getId();
    }

    // DELETE By Id
    public String deleteTag( String id ){
        TagId tagId = TagId.builder().id( id ).build();
        if(
                tagRepository.findById( tagId ).isPresent()
        ){
            tagRepository.delete( tagId );
            return "Tag with id " + id + " succesfully deleted.";
        }
        return "Tag with id " + id + " was not found.";
    }
}
