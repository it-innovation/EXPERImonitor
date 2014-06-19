/////////////////////////////////////////////////////////////////////////
//
// © University of Southampton IT Innovation Centre, 2013
//
// Copyright in this software belongs to University of Southampton
// IT Innovation Centre of Gamma House, Enterprise Road, 
// Chilworth Science Park, Southampton, SO16 7NS, UK.
//
// This software may not be used, sold, licensed, transferred, copied
// or reproduced in whole or in part in any manner or form or in or
// on any media by any person other than in accordance with the terms
// of the Licence Agreement supplied with the software, or otherwise
// without the prior written consent of the copyright owners.
//
// This software is distributed WITHOUT ANY WARRANTY, without even the
// implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
// PURPOSE, except where stated in the Licence Agreement supplied with
// the software.
//
//      Created By :            Dion Kitchener  
//      Created Date :          16-Aug-2013
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////



package uk.ac.soton.itinnovation.experimedia.arch.ecc.configRegistry.api;

/**
 * Interface to the main API methods for setting up a new project, and directory
 * and document management.
 * 
 */
public interface IECCDirectoryConfig{
  
   /**
    * Method that adds a new directory to the project repository.
    * The directoryName parameter must be followed by a forward slash, like so: "mydirectory/"
    * 
    * @param directoryName -  Name of the directory to be added
    * @throws Exception - Throws when directory name is NULL or does not exist in repository.
    */
    void addDirectory(String directoryName) throws Exception;
    
    /**
     * Method that deletes a directory in the project repository.
     * The directoryName parameter must include the entire path from the repository root directory
     * for example: "/ProjectName/DirectoryToDelete".  This would delete only the DirectoryToDelete
     * and not the ProjectName directory
     * 
     * @param directoryName Name of the directory to be deleted
     * @throws Exception - Throws when directory name is NULL or does not exist in repository.
     */
    void deleteDirectory(String directoryName)throws Exception;
    
    /**
     * Method that retrieves a document a specified document from the project repository and returns a string.
     * The filePath parameter must include the filename, extension and entire path from the repository root,
     * for example: "ProjectName/Docs/configDocument.txt"
     * 
     * @param filePath - The directory path from the repository, filename and extension of the document
     * @return UTF-8 encoded string
     * @throws Exception - Throws when file name is NULL or does not exist in repository.
     */
    String getDocument(String filePath)throws Exception; 
    
    /**
     * Method that enables a document to be uploaded to the project repository.
     * The source file path must be in the following format: "C://documents/myprojectdoc.txt".
     * The destination file path must include the filename, extension and  directory path for instance: 
     * "/ProjectName/ProjectDocs/myprojectdoc.txt".
     * The destination filename does not have to be the same, it can be whatever the user requires.  
     * However if a file of that name exists already in the destination directory then it will be overwritten by the new file.
     * 
     * @param sourceFilePath        - The full drive and directory path, filename and extension of the source file
     * @param destinationFilePath   - The directory path from the repository, filename and extension of the destination document
     * @throws Exception - Throws when either source or destination is NULL or the source does not exist in repository.
     */
    void putDocument( String sourceFilePath, String destinationFilePath )throws Exception;
    
    /**
     * Method to delete a document in the project repository.
     * The parameter file path must include the entire directory path from the repository root,
     * as well as the filename and extension, for example: "/ProjectName/Docs/Config/document.txt".
     *
     * @param filePath  -  The directory path from the repository, filename and extension of the document to be deleted
     * @throws Exception - Throws when either file name is NULL or does not exist in repository.
     */
    void deleteDocument(String filePath)throws Exception;
    
   /**
    * Method to move a document from one location to another in the project repository.
    * The source and destination file paths must include the entire path from the repository 
    * root,the filename and extension.  This method is similar to the copyDocument method in
    * that the destination filename can be different from the source, and if any file with
    * the same name exists in the destination directory it will be overwritten by the new file.
    * for example: "/ProjectName/Docs/document.txt".
    * 
    * @param sourceFilePath         - The full directory  path, filename and extension of the document to be moved
    * @param destinationFilePath    - The full directory  path, filename and extension of the document destination
    * @throws Exception - Throws when either file path is NULL or the source does not exist in repository.
    */
    void moveDocument(String sourceFilePath, String destinationFilePath)throws Exception;
    
    /**
     * Method to copy a document to another location in the project repository.
     * The source and destination file paths must include the entire path from the repository 
     * root,the filename and extension.  This method is similar to the moveDocument method in
     * that the destination filename can be different from the source, and if any file with
     * the same name exists in the destination directory it will be overwritten by the new file.
     * for example: "/ProjectName/Docs/document.txt".
     *
     * @param sourceFilePath        - The full directory  path, filename and extension of the document to be copied
     * @param destinationFilePath   - The full directory  path, filename and extension of the document destination
     * @throws Exception - Throws when either file path is NULL or the source does not exist in repository.
     */
    void copyDocument(String sourceFilePath, String destinationFilePath)throws Exception;
    
  
    
   /**
    * Method to check that a document or directory exists in the project repository and return a true or false value.
    * The parameter filePath must include the entire directory path from the repository root,
    * for example: "/ProjectName/Docs/Config/document.txt".
    * 
    * @param filePath   - The full directory  path, filename and extension of the document or directory to be checked
    * @return - true or false
    * @throws Exception - Throws when file path is NULL.
    */
    boolean documentExists(String filePath)throws Exception;
    
  
    
     /**
     *Method to set the location of a configuration repository on a local computer. 
     * 
     * @param localConfigPath - The drive and directory path of the local repository.
     * @throws Exception - Throws if a directory location can not be set.
     */
    void setLocalConfigPath(String localConfigPath) throws Exception;
    
    /**
     * Method to return the location of the configuration repository is one exists
     * 
     * @return  - The drive and directory path as a string, or null if it does not exist.
     * @throws Exception - Throws if local configuration repository does not exist.
     */
    public String getLocalConfigPath() throws Exception;
    
    /**
     * Method to create multiple local directories using a complete path including drive letter.
     * Will create any folder not already existing in the path.
     * 
     * @param directoryPath - The complete path including the drive letter.
     * @throws Exception  - Throws if the path is null.
     */
    public void createLocalRepository(String directoryPath) throws Exception;
    
    /**
     * Used to create a directory on the local computer.
     * 
     * @param directoryName  -  Name of the directory should only include directory names. 
     * @throws Exception - Throws if folder name is null.
     */
    void createLocalDirectory(String directoryName) throws Exception;
    
    
}